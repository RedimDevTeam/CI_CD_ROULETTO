package com.game.jwt;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
@Slf4j
public class JwtCipher {
    private static SecretKeySpec secretKey;
    private static byte[] key;
    private static final String ALGORITHM = "AES";

    public void prepareSecreteKey(String instanceName) {
        MessageDigest sha = null;
        try {
            String myKey = "ReDIM_ssg_v2.0";
            key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance(instanceName);
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    public String encrypt(String strToEncrypt, String instanceName) {
        try {
            prepareSecreteKey(instanceName);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return stringToHexadecimal(Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8"))));
        } catch (Exception e) {
            log.error("Error while encrypting: " + e);
        }
        return null;
    }

    public String decrypt(String strToDecrypt, String instanceName) {
        try {
            prepareSecreteKey(instanceName);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(hexadecimalToString(strToDecrypt))));
        } catch (Exception e) {
            log.error("Error while decrypting: " + e);
        }
        return null;
    }

    public static String stringToHexadecimal (String text) {
        StringBuilder sb = new StringBuilder();

        char[] ch = text.toCharArray();
        for(int i = 0; i < ch.length; i++) {
            String hexString = Integer.toHexString(ch[i]);
            sb.append(hexString);
        }
        String result = sb.toString();
        return result;
    }

    public static String hexadecimalToString(String text) {
        String result = new String();
        char[] charArray = text.toCharArray();
        for(int i = 0; i < charArray.length; i=i+2) {
            String st = ""+charArray[i]+""+charArray[i+1];
            char ch = (char)Integer.parseInt(st, 16);
            result = result + ch;
        }

        return result;
    }

    private final static String HASH_KEY = "lkjg;sdlf;saldkjf23;asljnf;213i545n;";
    public String encryptSha1(String strToDecrypt, String instanceName) {
        String hash = HASH_KEY + strToDecrypt;
        byte[] data = hash.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] easyHash;
        try {
            MessageDigest md = MessageDigest.getInstance(instanceName);
            easyHash = md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            // log.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return Base64.getEncoder().encodeToString(easyHash);
    }

}
