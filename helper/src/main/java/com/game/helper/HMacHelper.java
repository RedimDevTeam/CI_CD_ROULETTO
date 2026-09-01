package com.game.helper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class HMacHelper {
	public static String generateHmacSHA256(String secret, String data) {
		try {
			SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(secretKey);
			byte[] rawHmac = mac.doFinal(data.getBytes());
			return Base64.getEncoder().encodeToString(rawHmac);
		} catch (Exception e) {
			throw new RuntimeException("Failed to generate HMAC", e);
		}
	}

}
