package com.dowinn.rouletto.socket;

import com.dowinn.rouletto.communication.APIResponse;
import com.dowinn.rouletto.communication.StatusCode;
import com.dowinn.rouletto.redis.RedisHelper;
import com.dowinn.rouletto.service.SessionManager;
import com.dowinn.rouletto.util.DateTimeUtil;
import com.dowinn.rouletto.util.FunctionUtil;
import com.game.jwt.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SocketPool {

    private static ConcurrentHashMap<Long, SessionData> playerSessions = new ConcurrentHashMap<>();

    @Autowired
    JwtService jwtService;

    @Value("${session.expire}")
    private  Integer expire;

    @Autowired
    @Lazy
    RedisHelper redisHelper;

    @Autowired
    @Lazy
    SessionManager sessionManager;

    public SessionData add(WebSocketSession session) {

        Map<String, String> jwtValues = jwtService.verifyPlayer(session);
        Long playerId = Long.valueOf(jwtValues.get("playerId"));
        if (playerSessions.containsKey(playerId)) {
            SessionData sessionData = playerSessions.get(playerId);
            if (sessionData.getSession().isOpen()) {
                String data = FunctionUtil.Serialize(APIResponse.get(StatusCode.SESSION_EXPIRED));
                try {
                    sessionData.getSession().sendMessage(new TextMessage(data));
                    playerSessions.remove(playerId);
                } catch (IOException e) {
                    log.info("exception in sending message {}", e.getMessage());
                }
            }
        }
        if(!redisHelper.isSessionPresent(jwtValues.get("sessionId"))){
            String data = FunctionUtil.Serialize(APIResponse.get(StatusCode.SESSION_EXPIRED));
            try {
                session.sendMessage(new TextMessage(data));
            } catch (IOException e) {
                log.info("exception in sending message {}", e.getMessage());
            }
        }
        String tableId = jwtService.getTableId(session);
        Integer betLimit = Integer.valueOf(jwtService.getBetlimitId(session));
        String playerName = jwtValues.get("playername");
        String playerSessinoId = jwtValues.get("sessionId");
        String currency = jwtValues.get("currency");
        String casinoId = jwtValues.get("casinoId");
        String integrationType = jwtValues.get("integrationType");
        Integer rtp = Integer.valueOf(jwtValues.get("rtp"));
        SessionData sessionData = SessionData.builder()
                .userName(playerName)
                .playerSessionId(playerSessinoId)
                .betlimitId(betLimit)
                .casinoId(casinoId)
                .rtp(rtp)
                .currency(currency)
                .tableId(tableId)
                .playerId(playerId)
                .integrationType(integrationType)
                .socketSessionId(session.getId())
                .session(session)
                .sessionExpireTime(DateTimeUtil.addDelayInMinutes(expire))
                .build();
        playerSessions.put(playerId, sessionData);
        return sessionData;
    }

   public void sendMessage(String message, Long playerId) {

       SessionData sessionData = playerSessions.get(playerId);

       if (sessionData == null) {
           log.warn("No SessionData found for playerId={}", playerId);
           return;
       }

       WebSocketSession session = sessionData.getSession();

       if (session == null) {
           log.warn("WebSocket session is null for playerId={}", playerId);
           return;
       }

       if (!session.isOpen()) {
           log.warn("WebSocket session is closed for playerId={}", playerId);
           return;
       }

       try {
           synchronized (session) {
               session.sendMessage(new TextMessage(message));
           }
       } catch (IOException e) {
           log.error("Error sending message to player {}: {}", playerId, e.getMessage(), e);
       }
   }

    public void sendMessage(String message, String casinoId) {
        List<SessionData> sessions = playerSessions.values().stream().filter(a -> a.getCasinoId().equals(casinoId)).collect(Collectors.toList());

        for (SessionData sessionData : sessions) {
            WebSocketSession session = sessionData.getSession();
            try {
                if (session != null && session.isOpen()) {
                    synchronized (session) {
                        session.sendMessage(new TextMessage(message));
                    }
                }
            } catch (IOException e) {
                log.info("player {} exception in socket {}", sessionData.getPlayerId(), e.getMessage());
            }
        }
    }



    public void sendTableMessage(String message, String table) {
        List<SessionData> sessions = playerSessions.values().stream().filter(a -> a.getTableId().equals(table)).collect(Collectors.toList());

        for (SessionData sessionData : sessions) {
            WebSocketSession session = sessionData.getSession();
            if (session != null && session.isOpen()) {
                try {
                    synchronized (session) {
                        session.sendMessage(new TextMessage(message));
                    }
                } catch (IOException e) {
                    log.info("player {} exception in socket {}", sessionData.getPlayerId(), e);
                }
            }
        }
    }

    public void sendPlayerMessage(String message, String table, Set<Long> players) {
        List<SessionData> sessions = playerSessions.values().stream().filter(a -> (a.getTableId().equals(table) && !players.contains(a.getPlayerId()))).collect(Collectors.toList());

        for (SessionData sessionData : sessions) {
            WebSocketSession session = sessionData.getSession();
            if (session != null && session.isOpen()) {
                try {
                    synchronized (session) {
                        session.sendMessage(new TextMessage(message));
                    }
                } catch (IOException e) {
                    log.info("player {} exception in socket {}", sessionData.getPlayerId(), e);
                }
            }
        }
    }


    public void remove(WebSocketSession session) {
        SessionData sessionData = get(session);
        if (sessionData != null) {
            //redisHelper.removeSession(sessionData.getPlayerSessionId());
            playerSessions.remove(sessionData.getPlayerId());
        } else {
            log.info("session not present {}", session.getId());
        }
    }


    public void checkExpirySession() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                for (Map.Entry entry : playerSessions.entrySet()) {
                    java.lang.Long playerId = (Long) entry.getKey();
                    SessionData sessionData = get(playerId);
                    if (sessionData != null) {
                        LocalDateTime sessionExpireTime = sessionData.getSessionExpireTime();
                        if (DateTimeUtil.isAfter(sessionExpireTime)) {
                            WebSocketSession session = sessionData.getSession();
                            if (session.isOpen()) {
                                String data = FunctionUtil.Serialize(APIResponse.get(StatusCode.SESSION_EXPIRED));
                                try {
                                    log.info("session expired for player {}",playerId);
                                    sessionData.getSession().sendMessage(new TextMessage(data));
                                } catch (IOException e) {
                                    log.info("exception e {}", e);
                                }
                                remove(session);
                                sessionManager.endSession(sessionData);
                            }
                        }
                    }
                }
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 60 * 1000);
    }

    @Async
    public void updateSession(Long playerId, SessionData sessionData) {
        log.info("session updated");
        sessionData.setSessionExpireTime(DateTimeUtil.addDelayInMinutes(expire));
        playerSessions.put(playerId, sessionData);
    }

    public SessionData get(WebSocketSession session) {
        return playerSessions.values().stream().filter(a -> a.getSocketSessionId().equals(session.getId())).findFirst().orElse(null);
    }

    public SessionData get(Long playerId) {
        return playerSessions.get(playerId);
    }

    public void handlePing(WebSocketSession session, SessionData sessionData) {

        sendMessage(FunctionUtil.Serialize(APIResponse.get(StatusCode.WS_PONG)), sessionData.getPlayerId());

    }

    public List<SessionData> getCasinoPlayerList(String casino){
        return playerSessions.values().stream().filter(a -> a.getCasinoId().equals(casino)).collect(Collectors.toList());
    }
}
