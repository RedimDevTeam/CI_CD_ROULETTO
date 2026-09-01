package com.dowinn.rouletto.socket;


import com.dowinn.rouletto.enums.SocketMessageStatus;
import com.dowinn.rouletto.enums.SocketSessionStatus;
import com.dowinn.rouletto.service.PlayerActionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
@Slf4j
public class SocketService {

    @Autowired
    SocketPool socketPool;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PlayerActionService playerAction;


    public void handleWebsocketMessage(WebSocketSession session, TextMessage message, SocketSessionStatus status) {

        try {
            switch (status) {
                case EASTABLISH -> socketInit(session);
                case MESSAGES -> handleMessage(session, message);
                case CLOSE -> socketClose(session);
            }
        } catch (Exception e) {
            log.error("WebSocket error for playerId {}", e);
        }

    }

    public void socketInit(WebSocketSession session) {
        SessionData sessionData = socketPool.add(session);
        playerAction.handlePlayerInit(session, sessionData);
    }

    public void handleMessage(WebSocketSession session, TextMessage message) {

        try {
            JsonNode jsonNode = objectMapper.readTree(message.asBytes());
            SocketMessageStatus status = SocketMessageStatus.valueOf(jsonNode.get("action").asText());
            String values = jsonNode.get("values").toString();
            SessionData sessionData = socketPool.get(session);
            if (sessionData != null) {
                //log.info("playerId:: {} status::{} message :: {}", sessionData.getPlayerId(), status, values);
                switch (status) {
                    case CONFIRM_BET -> playerAction.handleConfirmBet(session, values, sessionData);
                    case BALANCE -> playerAction.getBalance(session, sessionData);
                    case WS_PING -> socketPool.handlePing(session, sessionData);
                }
            }
        } catch (Exception e) {
            log.info("exception {}",e.getMessage());
        }

    }

    public void socketClose(WebSocketSession session) {
        socketPool.remove(session);
    }


}
