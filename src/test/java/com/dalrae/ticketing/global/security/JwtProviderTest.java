package com.dalrae.ticketing.global.security;

import com.dalrae.ticketing.global.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtProvider")
class JwtProviderTest {

    private static final String SECRET_KEY = "wkwP5PsUMjeA8PqoMe6PzotiSAgWanskHWNjtL1AYmU=";
    private static final Duration ACCESS_VALIDITY = Duration.ofMinutes(30);
    private static final Duration REFRESH_VALIDITY = Duration.ofDays(7);
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";
    private final JwtProvider jwtProvider = new JwtProvider(new JwtProperties(SECRET_KEY, ACCESS_VALIDITY, REFRESH_VALIDITY));
    private final UUID userId = UUID.randomUUID();

    // 토큰 생성
    @Test
    @DisplayName("액세스 토큰을 생성하고 다시 풀어서 값이 같은지 검증한다.")
    void givenUserIdAndRole_whenCreateAccessToken_thenContainsUserIdAndRoleAndTypeAndExpiration() {
        String accessToken = jwtProvider.createAccessToken(userId, Role.USER);
        Claims claims = jwtProvider.parseAccessToken(accessToken);

        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
        assertThat(claims.get(CLAIM_ROLE, String.class)).isEqualTo(Role.USER.name());
        assertThat(claims.get(CLAIM_TYPE, String.class)).isEqualTo(TYPE_ACCESS);
    }

    @Test
    @DisplayName("리프레시 토큰을 생성하고 다시 풀어서 값이 같은지 검증한다.")
    void givenUserId_whenCreateRefreshToken_thenContainsUserIdAndTypeAndExpiration() {
        String refreshToken = jwtProvider.createRefreshToken(userId);
        Claims claims = jwtProvider.parseRefreshToken(refreshToken);

        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
        assertThat(claims.get(CLAIM_TYPE, String.class)).isEqualTo(TYPE_REFRESH);
        assertThat(claims.get(CLAIM_ROLE)).isNull();
    }

    @Test
    @DisplayName("리프레시 토큰에는 발급마다 고유한 id(jti)가 담긴다.")
    void givenSameUser_whenCreateRefreshTokenTwice_thenIdsDiffer() {
        Claims claims1 = jwtProvider.parseRefreshToken(jwtProvider.createRefreshToken(userId));
        Claims claims2 = jwtProvider.parseRefreshToken(jwtProvider.createRefreshToken(userId));

        assertThat(claims1.getId()).isNotBlank();
        assertThat(claims1.getId()).isNotEqualTo(claims2.getId());
    }

    // 토큰 타입 검사
    @Test
    @DisplayName("액세스 토큰을 리프레시 토큰으로 파싱하면 예외가 발생한다.")
    void givenAccessToken_whenParseRefreshToken_thenThrowsJwtException() {
        String accessToken = jwtProvider.createAccessToken(userId, Role.USER);

        assertThatThrownBy(() -> jwtProvider.parseRefreshToken(accessToken))
                .isInstanceOf(JwtException.class)
                .hasMessage("토큰 타입이 올바르지 않습니다.");
    }

    @Test
    @DisplayName("리프레시 토큰을 액세스 토큰으로 파싱하면 예외가 발생한다.")
    void givenRefreshToken_whenParseAccessToken_thenThrowsJwtException() {
        String refreshToken = jwtProvider.createRefreshToken(userId);

        assertThatThrownBy(() -> jwtProvider.parseAccessToken(refreshToken))
                .isInstanceOf(JwtException.class)
                .hasMessage("토큰 타입이 올바르지 않습니다.");
    }

    // 토큰 위조, 손상 검사
    @Test
    @DisplayName("만료된 토큰은 예외가 발생한다.")
    void givenExpireToken_whenParseAccessToken_thenThrowsExpiredJwtException() {
        JwtProvider expiredProvider = new JwtProvider(new JwtProperties(SECRET_KEY, Duration.ofSeconds(-1), REFRESH_VALIDITY));
        String accessToken = expiredProvider.createAccessToken(userId, Role.USER);

        assertThatThrownBy(() -> jwtProvider.parseAccessToken(accessToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("다른 키로 서명된 토큰은 예외가 발생한다.")
    void givenTokenSignedWithOtherKey_whenParseAccessToken_thenThrowsJwtException() {
        String otherKey = "WF+IzrLkBr/+fOMyfDS4mW+Q0NH+paoupchWGARvttM=";
        JwtProvider otherKeyProvider = new JwtProvider(new JwtProperties(otherKey, ACCESS_VALIDITY, REFRESH_VALIDITY));
        String accessToken = otherKeyProvider.createAccessToken(userId, Role.USER);

        assertThatThrownBy(() -> jwtProvider.parseAccessToken(accessToken))
                .isInstanceOf(JwtException.class);
    }

    // 키 검증
    @Test
    @DisplayName("키가 256비트보다 짧으면 JwtProvider를 생성할 수 없다.")
    void givenShortKey_whenCreateProvider_thenThrowsWeakKeyException() {
        String shortKey = "c2hvcnQta2V5";
        assertThatThrownBy(() -> new JwtProvider(new JwtProperties(shortKey, ACCESS_VALIDITY, REFRESH_VALIDITY)))
                .isInstanceOf(WeakKeyException.class);
    }
}