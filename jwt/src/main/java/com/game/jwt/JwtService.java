package com.game.jwt;


import com.game.helper.SocketStatus;
import com.game.jwt.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class JwtService {

    public Map<String, String> verifyPlayer(WebSocketSession session) {
        // Extract the JWT values from the session
        Map<String, String> jwtValues = getJWTValues(session);
        log.info("verify jwt values {}",jwtValues);
        if (jwtValues.containsKey("status")) {
            return jwtValues;
        }
        // Verify the player session based on the extracted JWT values
        verifyPlayer(jwtValues);
        return jwtValues;
    }


    private Map<String, String> verifyPlayer(Map<String, String> jwtValues) {
        if (!jwtValues.isEmpty()) {
            Long playerId = null;
            String sessionId = null;
            try {
                // Extract player ID and session ID from JWT values
                if (jwtValues.containsKey("playerId") && jwtValues.containsKey("sessionId")) {
                    playerId = Long.parseLong(jwtValues.get("playerId"));
                    sessionId = jwtValues.get("sessionId");

                    log.info("playerId {}  sessionId {}",playerId,sessionId);
                }
            } catch (Exception ignored) {
                // If error occurs during extraction, mark the session as expired
                jwtValues.put("status", SocketStatus.SESSION_EXPIRED.name());
                return jwtValues;
            }

            // If player ID and session ID are valid
            if (playerId != null && sessionId != null) {
                // For normal player, check the player session
                log.info("isValid");
                boolean isValid =true;
                if (isValid) {
                    jwtValues.put("status", SocketStatus.SUCCESS.name());
                    return jwtValues;
                }
            }
        }

        log.info("Invalid");
        // If no valid session, mark as expired
        jwtValues.put("status", SocketStatus.SESSION_EXPIRED.name());
        return jwtValues;
    }


    private Map<String, String> getJWTValues(WebSocketSession session) {
        // Check if the session URI has a query string
        if (Objects.requireNonNull(session.getUri()).getQuery() != null) {
            // Extract query parameters from the URI
            Map<String, String> queryValues = getQueryMap(session.getUri());

           // log.info("query values {}",queryValues);
            // If a token is present in the query, decode it
            if (queryValues.containsKey("token")) {
                return JwtTokenUtil.getDataByToken(queryValues.get("token"));
            }
        }
        // Log and return an invalid query string status if no token is found
        log.info("invalid query string");
        return Map.of("status", SocketStatus.INVALID_QUERY_STRING.name());
    }


    private static Map<String, String> getQueryMap(URI uri) {
        // Split the query string into key-value pairs
        String[] params = uri.getQuery().split("&");
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            try {
                // Split each parameter by '=' and put into the map
                String[] keyAndValue = param.split("=");
                map.put(keyAndValue[0], keyAndValue[1]);
            } catch (Exception ignored) {
                // Ignore invalid query parameters
            }
        }
        return map;
    }


    //todo check valid connection
    private boolean isValidConnection(long userId, String sessionId) {
        String sessionResponse = null;
        try {
            // Check the player session status through the gateway service
            //  sessionResponse = gatewayService.checkPlayerSession(userId, sessionId).getBody();
        } catch (Exception ignored) {
            // Catch any exception during session check
        }
        // Return true if session response is successful, otherwise false
        if (sessionResponse != null) {
            return Objects.equals(sessionResponse, "SUCCESS");
        }
        return true;
    }

    public Long getPlayerId(WebSocketSession session) {
        // Extract JWT values from the session
        Map<String, String> jwtValues = getJWTValues(session);

        // If the query string is invalid, return null
        if (Objects.equals(jwtValues.get("status"), SocketStatus.INVALID_QUERY_STRING.name())) {
            return null;
        }
        // If player ID is present in the JWT values, return it
        if (jwtValues.containsKey("playerId")) {
            try {
                return Long.parseLong(jwtValues.get("playerId"));
            } catch (Exception ignored) {
                // If parsing fails, return null
                return null;
            }
        }
        return null;
    }
    public String getTableId(WebSocketSession session) {
        // Check if the session URI has a query string
        if (Objects.requireNonNull(session.getUri()).getQuery() != null) {
            // Extract query parameters from the URI
            Map<String, String> queryValues = getQueryMap(session.getUri());

          //  log.info("query values {}",queryValues);
            // If a token is present in the query, decode it
            if (queryValues.containsKey("tableId")) {
                return queryValues.get("tableId");
            }
        }
        // Log and return an invalid query string status if no token is found
        log.info("invalid query string");
        return null;
    }

    public String getBetlimitId(WebSocketSession session) {
        // Check if the session URI has a query string
        if (Objects.requireNonNull(session.getUri()).getQuery() != null) {
            // Extract query parameters from the URI
            Map<String, String> queryValues = getQueryMap(session.getUri());

            //  log.info("query values {}",queryValues);
            // If a token is present in the query, decode it
            if (queryValues.containsKey("classicBetLimitTypeId")) {
                return queryValues.get("classicBetLimitTypeId");
            }
        }
        // Log and return an invalid query string status if no token is found
        log.info("invalid query string");
        return null;
    }
}