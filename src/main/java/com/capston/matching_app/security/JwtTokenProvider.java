package com.capston.matching_app.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {
    private final Key key;
    private final long accessValidityMillis;
    private final long refreshValidityMillis;


    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-seconds}") long accessValiditySeconds,
            @Value("${jwt.refresh-token-validity-seconds}") long refreshValiditySeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessValidityMillis = accessValiditySeconds * 1000L;
        this.refreshValidityMillis = refreshValiditySeconds * 1000L;
    }


    public String createAccessToken(String subject, Map<String, Object> claims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + accessValidityMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    public String createRefreshToken(String subject) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + refreshValidityMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}
