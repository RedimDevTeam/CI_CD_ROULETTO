package com.game.helper;

import jakarta.servlet.http.HttpServletRequest;
import ua_parser.Client;
import ua_parser.Parser;

import java.util.Objects;

public class DeviceHelper {
	private static final String UNKNOWN = "UNKNOWN";

	public static String[] getDetails(HttpServletRequest request) {
		String ip = extractIp(request);
		String hostName = request.getServerName();
		
		String device = getDeviceDetails(request.getHeader("user-agent"));
		// System.out.println(ip);
		// System.out.println(device);
		return new String[] { ip, device, hostName };
	}

	private static String extractIp(HttpServletRequest request) {
		String clientIp;
		String clientXForwardedForIp = request.getHeader("X-Forwarded-For");
		if (Objects.nonNull(clientXForwardedForIp)) {
			clientIp = parseXForwardedHeader(clientXForwardedForIp);
		} else {
			clientIp = request.getRemoteAddr();
		}

		return clientIp;
	}

	private static String parseXForwardedHeader(String header) {
		return header.split(" *, *")[0];
	}

	private static String getDeviceDetails(String userAgent) {
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
