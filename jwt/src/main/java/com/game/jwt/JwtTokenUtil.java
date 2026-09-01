package com.game.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = -2550185165626007488L;

    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;

    private static final String subject = "UserDetails";
    private static final String secret = "gamecasino";
    private static final String issuer = "GameCasino/game/Casino";

//
//    public String getUsernameFromToken(String token) {
//        return getClaimFromToken(token, Claims::getSubject);
//    }
//
//    public Date getIssuedAtDateFromToken(String token) {
//        return getClaimFromToken(token, Claims::getIssuedAt);
//    }
//
//    public Date getExpirationDateFromToken(String token) {
//        return getClaimFromToken(token, Claims::getExpiration);
//    }
//
//    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = getAllClaimsFromToken(token);
//        return claimsResolver.apply(claims);
//    }
//
//    private Claims getAllClaimsFromToken(String token) {
//        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
//    }
//
//    private Boolean isTokenExpired(String token) {
//        final Date expiration = getExpirationDateFromToken(token);
//        return expiration.before(new Date());
//    }
//
//    private Boolean ignoreTokenExpiration(String token) {
//        // here you specify tokens, for that the expiration is ignored
//        return false;
//    }
//
//    public String generateToken(UserDetails userDetails) {
//        Map<String, Object> claims = new HashMap<>();
//        return doGenerateToken(claims, userDetails.getUsername());
//    }
//
//    private String doGenerateToken(Map<String, Object> claims, String subject) {
//
//        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY*1000)).signWith(SignatureAlgorithm.HS512, secret).compact();
//    }
//
//    public Boolean canTokenBeRefreshed(String token) {
//        return (!isTokenExpired(token) || ignoreTokenExpiration(token));
//    }
//
//    public Boolean validateToken(String token, UserDetails userDetails) {
//        final String username = getUsernameFromToken(token);
//        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
//    }

    public static String generateToken(HashMap<String, String> values, String jwtUserDetailsUsername)
            throws IllegalArgumentException, JWTCreationException {

        JWTCreator.Builder builder = JWT.create().withSubject(subject);

        if(jwtUserDetailsUsername != null) {
            builder.withClaim("JwtUserDetailsUsername", jwtUserDetailsUsername);
        }

        for (Map.Entry<String,String> entry : values.entrySet()) {
            builder.withClaim(entry.getKey(), entry.getValue());
        }

        String token = builder.withIssuedAt(new Date())
                .withIssuer(issuer).sign(Algorithm.HMAC256(secret));

        return encrypt(token);
    }

    public static HashMap<String, String> getData(String authHeader) {

        String token = authHeader.substring(7);

        return getDataByToken(token);
    }

    public static HashMap<String, String> getDataByToken(String encryptToken) {

        String token = decrypt(encryptToken);

        HashMap<String, String> values = new HashMap<String, String>();
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).withSubject(subject)
                    .withIssuer(issuer).build();
            DecodedJWT jwt = verifier.verify(token);

            Map<String, Claim> claimMap = jwt.getClaims();

            for (Map.Entry<String, Claim> entry : claimMap.entrySet()) {
                switch (entry.getKey()) {
                    case "sub", "iss", "lat" -> {

                    }
                    default -> {
                        values.put(entry.getKey(), entry.getValue().asString());
                    }
                }
            }
        } catch (Exception e) {

        }
        return values;
    }

    public static DecodedJWT getDecodedJWT(String encryptToken) {
        String token = decrypt(encryptToken);
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).withSubject(subject).withIssuer(issuer)
                .build();
        return verifier.verify(token);
    }

    public static String encrypt(String strToEncrypt) {
        JwtCipher jwtCipher = new JwtCipher();
        return jwtCipher.encrypt(strToEncrypt, "SHA-1");
    }

    public static String decrypt(String strToDecrypt) {
        JwtCipher jwtCipher = new JwtCipher();
        return jwtCipher.decrypt(strToDecrypt, "SHA-1");
    }

    public static String encryptSha(String strToEncrypt) {
        JwtCipher jwtCipher = new JwtCipher();
        return jwtCipher.encryptSha1(strToEncrypt, "SHA-1");
    }

}
