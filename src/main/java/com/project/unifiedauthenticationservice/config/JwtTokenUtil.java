package com.project.unifiedauthenticationservice.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenUtil {
    private SecretKey secretKey;

    public JwtTokenUtil() {
        this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    private final long EXPIRATION_TIME = 86400000;

    public String generateToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey) //ES256
                .compact();
    }
}

//    public String generateToken(String username) {
//        Map<String, Object> claims = new HashMap<>();
//        return createToken(claims, username);
//    }
//
//    private String createToken(Map<String, Object> claims, String subject) {
//        Date now = new Date();
//        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
//
//        JwtBuilder builder = Jwts.builder()
//                .setClaims(claims)
//                .setSubject(subject)
//                .setIssuedAt(now)
//                .setExpiration(expiryDate)
//                .signWith(SignatureAlgorithm.HS256, SECRET_KEY);
//
//        return builder.compact();
//    }
//
//    public boolean validateToken(String token, String username) {
//        final String extractedUsername = extractUsername(token);
//        return (extractedUsername.equals(username) && !isTokenExpired(token));
//    }
//
//    private String extractUsername(String token) {
//        return extractAllClaims(token).getSubject();
//    }
//
//    private Claims extractAllClaims(String token) {
//        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
//    }
//
//    private boolean isTokenExpired(String token) {
//        return extractAllClaims(token).getExpiration().before(new Date());
//    }
