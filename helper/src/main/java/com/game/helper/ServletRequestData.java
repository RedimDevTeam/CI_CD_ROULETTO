package com.game.helper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import ua_parser.Client;
import ua_parser.Parser;

import java.util.Objects;

@Slf4j
public class ServletRequestData {

    private static final String UNKNOWN = "UNKNOWN";

    public static String getIp(HttpServletRequest servletRequest) {
        String clientIp;
        String clientXForwardedForIp = servletRequest.getHeader("X-Forwarded-For");
        if (Objects.nonNull(clientXForwardedForIp)) {
            clientIp = parseXForwardedHeader(clientXForwardedForIp);
            log.info("clientIp: " + clientIp);
        } else {
            clientIp = servletRequest.getRemoteAddr();
            log.info("Remote clientIp: " + clientIp);
        }

        return clientIp;
    }

    private static String parseXForwardedHeader(String header) {
        return header.split(" *, *")[0];
    }

    public static String getUserAgent(HttpServletRequest servletRequest) {

        String userAgent = servletRequest.getHeader("user-agent");

        String deviceDetails = UNKNOWN;
        Parser parser = new Parser();
        Client client = parser.parse(userAgent);
        if (Objects.nonNull(client)) {
            deviceDetails = client.userAgent.family + " " + client.userAgent.major + "." + client.userAgent.minor
                    + " - " + client.os.family + " " + client.os.major + "." + client.os.minor;
        }

        return deviceDetails;
    }

}
