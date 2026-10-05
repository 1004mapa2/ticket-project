package com.dalrae.ticketing.global.security;

import com.dalrae.ticketing.global.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final Duration accessTokenValidity;
    @Getter
    private final Duration refreshTokenValidity;

    public JwtProvider(JwtProperties jwtProperties) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
        this.accessTokenValidity = jwtProperties.getAccessTokenValidity();
        this.refreshTokenValidity = jwtProperties.getRefreshTokenValidity();
    }

    public String createAccessToken(UUID userId, Role role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenValidity.toMillis()))
                .claim(CLAIM_ROLE, role.name())
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(UUID userId) {
        Date now = new Date();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId.toString())
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenValidity.toMillis()))
                .signWith(key)
                .compact();
    }

    public Claims parseAccessToken(String token) {
        return parse(token, TYPE_ACCESS);
    }

    public Claims parseRefreshToken(String token) {
        return parse(token, TYPE_REFRESH);
    }

    private Claims parse(String token, String type) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        if (!type.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new JwtException("토큰 타입이 올바르지 않습니다.");

        }
        return claims;
    }
}
