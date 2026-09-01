package com.game.helper;

import java.math.BigInteger;
import java.security.MessageDigest;

public class PasswordHelper {
	public static String md5Hash(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			byte[] messageDigest = md.digest(password.getBytes());

			BigInteger no = new BigInteger(1, messageDigest);

			String hashtext = no.toString(16);
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;
		} catch (Exception ex) {
			return password;
		}
	}
}
