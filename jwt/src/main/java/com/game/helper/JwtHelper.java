package com.game.helper;

import com.game.jwt.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class JwtHelper {
    private final HashMap<String, String> values;
    @Override
    public String toString() {
        return "JwtHelper{" +
                "values=" + values +
                ", valueSize=" + valueSize +
                '}';
    }

    private final int valueSize;
    public JwtHelper(String authHeader) {
        values = JwtTokenUtil.getData(authHeader);
        valueSize = values.size();
    }

    private String getString(String key) {
        if(valueSize > 0) {
            return values.get(key);
        }
        return "";
    }

    private long getLong(String key) {
        if(valueSize > 0) {
            try {
                return Long.parseLong(values.get(key));
            } catch (Exception ignored) {

            }
        }
        return -1;
    }

    public String getUserType() {
        return getString("type");
    }

    public long getUserId() {
        return getLong("playerId");
    }


    public String getCasinoId() {
        return getString("casinoId");
    }

    public String getFirstName() {
        return getString("firstName");
    }

    public String getLastName() {
        return getString("lastName");
    }

    public String getSessionId() {
        return getString("sessionId");
    }

    public String getTableId() {
        return getString("tableId");
    }

    public String getShortId() {
        return getString("shortId");
    }

    public String getIP() {
        return getString("ip");
    }

    public String getIntegrationTypeId() {
        return getString("integrationTypeId");
    }

    public String getCurrency() {
        return getString("currency");
    }


    public static String generatePlayerToken(long id, String casinoId, String sessionId, String ip, String firstName,
                                    String lastName, String integrationType, String currency, String avatar, String countryCode) {
        return JwtHelper.generateUserToken("User", id, casinoId, "table", "short", sessionId, ip, firstName, lastName, integrationType, currency, avatar, countryCode);
    }

    public static String generateDealerToken(long id, String tableId, String shortId, String sessionId, String ip, String firstName,
                                      String lastName, String studioId)  {
        return JwtHelper.generateUserToken("Dealer", id, studioId, tableId, shortId, sessionId, ip, firstName, lastName, "NONE", "CUR", "", "");
    }


    private static String generateUserToken(String type, long id, String casinoId, String tableId, String shortId, String sessionId, String ip, String firstName,
                                             String lastName, String integrationType, String currency, String avatar, String countryCode) {

        HashMap<String, String> values = new HashMap<String, String>();
        values.put("type", type);
        values.put("id", String.valueOf(id));
        values.put("casinoId", casinoId);
        values.put("sessionId", sessionId);
        values.put("tableId", tableId);
        values.put("shortId", shortId);
        values.put("firstName", firstName);
        values.put("lastName", lastName);
        values.put("ip", ip);
        values.put("integrationType", integrationType);
        values.put("currency", currency);
        values.put("avatar", Objects.requireNonNullElse(avatar, ""));
        values.put("countryCode", countryCode);

        return JwtTokenUtil.generateToken(values, String.valueOf(id));
    }

    public static Map<String, String> getLoggingFilterData(HttpServletRequest request) {
        Map<String, String> values = new HashMap<>();
        String authTokenHeader = request.getHeader("Authorization");

        if(authTokenHeader == null) {

            values.put("userId","");
            values.put("casinoId", "");
            values.put("tableId", "");
            values.put("userType", "");

            return values;
        }

        JwtHelper jwtHelper = new JwtHelper(authTokenHeader);

        values.put("userId",String.valueOf(jwtHelper.getUserId()));
        values.put("casinoId", jwtHelper.getCasinoId());
        values.put("tableId", jwtHelper.getTableId());
        values.put("userType", jwtHelper.getUserType());

        return  values;
    }


    public Map<String, String> verifyPlayer(WebSocketSession session) {
        // Extract the JWT values from the session
        Map<String, String> jwtValues = getJWTValues(session);
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
                if (jwtValues.containsKey("id") && jwtValues.containsKey("sessionId")) {
                    playerId = Long.parseLong(jwtValues.get("id"));
                    sessionId = jwtValues.get("sessionId");
                }
            } catch (Exception ignored) {
                // If error occurs during extraction, mark the session as expired
                jwtValues.put("status", SocketStatus.SESSION_EXPIRED.name());
                return jwtValues;
            }

            // If player ID and session ID are valid
            if (playerId != null && sessionId != null) {
                // For normal player, check the player session
                    boolean isValid =true;
                    if (isValid) {
                        jwtValues.put("status", SocketStatus.SUCCESS.name());
                        return jwtValues;
                    }
                }
            }

        // If no valid session, mark as expired
        jwtValues.put("status", SocketStatus.SESSION_EXPIRED.name());
        return jwtValues;
    }


    private Map<String, String> getJWTValues(WebSocketSession session) {
        // Check if the session URI has a query string
        if (Objects.requireNonNull(session.getUri()).getQuery() != null) {
            // Extract query parameters from the URI
            Map<String, String> queryValues = getQueryMap(session.getUri());
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
}
