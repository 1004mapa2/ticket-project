package com.dalrae.ticketing.auth.service;

import com.dalrae.ticketing.auth.dto.LoginRequest;
import com.dalrae.ticketing.auth.dto.ReissueRequest;
import com.dalrae.ticketing.auth.dto.TokenResponse;
import com.dalrae.ticketing.auth.repository.InMemoryRedisRepository;
import com.dalrae.ticketing.auth.repository.RedisRepository;
import com.dalrae.ticketing.global.Role;
import com.dalrae.ticketing.global.exception.BusinessException;
import com.dalrae.ticketing.global.exception.ErrorCode;
import com.dalrae.ticketing.global.security.JwtProperties;
import com.dalrae.ticketing.global.security.JwtProvider;
import com.dalrae.ticketing.user.domain.User;
import com.dalrae.ticketing.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("AuthService")
class AuthServiceTest {

    private static final String SECRET_KEY = "wkwP5PsUMjeA8PqoMe6PzotiSAgWanskHWNjtL1AYmU=";
    private static final String EMAIL = "test@dalrae.com";
    private static final String RAW_PASSWORD = "a1234";
    private static final Duration ACCESS_VALIDITY = Duration.ofMinutes(30);
    private static final Duration REFRESH_VALIDITY = Duration.ofDays(7);

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final RedisRepository redisRepository = new InMemoryRedisRepository();
    private final JwtProvider jwtProvider = new JwtProvider(new JwtProperties(SECRET_KEY, ACCESS_VALIDITY, REFRESH_VALIDITY));
    private final AuthService authService = new AuthService(passwordEncoder, jwtProvider, userRepository, redisRepository);
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void before() {
        User user = User.builder()
                .email(EMAIL)
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("park")
                .phone("01011112222")
                .build();
        ReflectionTestUtils.setField(user, "userId", userId);
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
    }

    @Test
    @DisplayName("로그인하면 검증 가능한 액세스 토큰을 발급하고 리프레시 토큰을 저장한다.")
    void whenLoginWithValidCredentials_thenIssuesVerifiableTokensAndStoresRefreshToken() {
        TokenResponse response = authService.login(new LoginRequest(EMAIL, RAW_PASSWORD));

        Claims accessClaims = jwtProvider.parseAccessToken(response.accessToken());
        assertThat(accessClaims.getSubject()).isEqualTo(userId.toString());
        assertThat(accessClaims.get("role", String.class)).isEqualTo("USER");

        Claims refreshClaims = jwtProvider.parseRefreshToken(response.refreshToken());
        assertThat(refreshClaims.getSubject()).isEqualTo(userId.toString());

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(redisRepository.find(userId)).contains(response.refreshToken());
    }

    @Test
    @DisplayName("존재하지 않는 이메일이면 로그인에 실패하고 리프레시 토큰을 저장하지 않는다.")
    void whenLoginWithUnknownEmail_thenThrowsLoginFailed() {
        BusinessException e = assertThrows(BusinessException.class, () -> authService.login(new LoginRequest("unknown@dalrae.com", RAW_PASSWORD)));

        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.LOGIN_FAILED);
        assertThat(redisRepository.find(userId)).isEmpty();
    }

    @Test
    @DisplayName("비밀번호가 틀리면 로그인에 실패하고 리프레시 토큰을 저장하지 않는다.")
    void whenLoginWithWrongPassword_thenThrowsLoginFailed() {
        BusinessException e = assertThrows(BusinessException.class, () -> authService.login(new LoginRequest(EMAIL, "wrong-password")));

        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.LOGIN_FAILED);
        assertThat(redisRepository.find(userId)).isEmpty();
    }

    @Test
    @DisplayName("재발급하면 새 토큰을 발급하고 저장된 리프레시 토큰을 새 토큰으로 교체한다.")
    void whenReissueWithValidRefreshToken_thenRotatesRefreshToken() {
        TokenResponse login = authService.login(new LoginRequest(EMAIL, RAW_PASSWORD));

        TokenResponse reissued = authService.reissue(new ReissueRequest(login.refreshToken()));

        assertThat(jwtProvider.parseAccessToken(reissued.accessToken()).getSubject()).isEqualTo(userId.toString());
        assertThat(reissued.refreshToken()).isNotEqualTo(login.refreshToken());
        assertThat(redisRepository.find(userId)).contains(reissued.refreshToken());
    }

    @Test
    @DisplayName("교체된 리프레시 토큰을 재사용하면 새 리프레시 토큰까지 모두 무효화된다.")
    void whenReissueWithReusedRefreshToken_thenRevokesAllRefreshTokens() {
        TokenResponse login = authService.login(new LoginRequest(EMAIL, RAW_PASSWORD));
        TokenResponse reissued = authService.reissue(new ReissueRequest(login.refreshToken()));

        BusinessException reused = assertThrows(BusinessException.class,
                () -> authService.reissue(new ReissueRequest(login.refreshToken())));
        assertThat(reused.getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
        assertThat(redisRepository.find(userId)).isEmpty();

        BusinessException revoked = assertThrows(BusinessException.class,
                () -> authService.reissue(new ReissueRequest(reissued.refreshToken())));
        assertThat(revoked.getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("저장된 리프레시 토큰이 없으면 재발급에 실패한다.")
    void whenReissueWithoutStoredRefreshToken_thenThrowsInvalidRefreshToken() {
        TokenResponse login = authService.login(new LoginRequest(EMAIL, RAW_PASSWORD));
        redisRepository.delete(userId);

        BusinessException e = assertThrows(BusinessException.class,
                () -> authService.reissue(new ReissueRequest(login.refreshToken())));

        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("액세스 토큰으로 재발급하면 실패하고 저장된 리프레시 토큰은 유지된다.")
    void whenReissueWithAccessToken_thenThrowsAndKeepsStoredRefreshToken() {
        TokenResponse login = authService.login(new LoginRequest(EMAIL, RAW_PASSWORD));

        BusinessException e = assertThrows(BusinessException.class,
                () -> authService.reissue(new ReissueRequest(login.accessToken())));

        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
        assertThat(redisRepository.find(userId)).contains(login.refreshToken());
    }

    @Test
    @DisplayName("만료된 리프레시 토큰은 저장되어 있어도 재발급에 실패한다.")
    void whenReissueWithExpiredRefreshToken_thenThrowsInvalidRefreshToken() {
        JwtProvider expiredProvider =
                new JwtProvider(new JwtProperties(SECRET_KEY, ACCESS_VALIDITY, Duration.ofSeconds(-1)));
        String expiredToken = expiredProvider.createRefreshToken(userId);
        redisRepository.save(userId, expiredToken, REFRESH_VALIDITY);

        BusinessException e = assertThrows(BusinessException.class,
                () -> authService.reissue(new ReissueRequest(expiredToken)));

        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("회원이 삭제되었으면 재발급에 실패하고 저장된 리프레시 토큰을 삭제한다.")
    void whenReissueForDeletedUser_thenThrowsAndDeletesStoredRefreshToken() {
        TokenResponse login = authService.login(new LoginRequest(EMAIL, RAW_PASSWORD));
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        BusinessException e = assertThrows(BusinessException.class,
                () -> authService.reissue(new ReissueRequest(login.refreshToken())));

        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
        assertThat(redisRepository.find(userId)).isEmpty();
    }

    @Test
    @DisplayName("재발급하면 DB의 현재 권한으로 액세스 토큰을 발급한다.")
    void givenRoleChanged_whenReissue_thenAccessTokenHasNewRole() {
        TokenResponse login = authService.login(new LoginRequest(EMAIL, RAW_PASSWORD));
        User user = userRepository.findById(userId).orElseThrow();
        ReflectionTestUtils.setField(user, "role", Role.ADMIN);

        TokenResponse reissued = authService.reissue(new ReissueRequest(login.refreshToken()));

        assertThat(jwtProvider.parseAccessToken(reissued.accessToken()).get("role", String.class))
                .isEqualTo("ADMIN");
    }

    @Nested
    @DisplayName("로그아웃")
    class logout {
        @Test
        @DisplayName("저장된 리프레시 토큰을 삭제한다.")
        void deleteStoredRefreshToken() {
            authService.login(new LoginRequest(EMAIL, RAW_PASSWORD));

            authService.logout(userId);

            assertThat(redisRepository.find(userId)).isEmpty();
        }

        @Test
        @DisplayName("로그아웃한 뒤에는 리프레시 토큰으로 재발급할 수 없다.")
        void cannotReissue() {
            TokenResponse login = authService.login(new LoginRequest(EMAIL, RAW_PASSWORD));
            authService.logout(userId);

            BusinessException e = assertThrows(BusinessException.class,
                    () -> authService.reissue(new ReissueRequest(login.refreshToken())));

            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        @Test
        @DisplayName("저장된 리프레시 토큰이 없어도 로그아웃은 예외 없이 처리된다.")
        void givenNoStoredRefreshToken_whenLogout_thenDoesNotThrow() {
            assertDoesNotThrow(() -> authService.logout(userId));
            assertDoesNotThrow(() -> authService.logout(userId));

            assertThat(redisRepository.find(userId)).isEmpty();
        }
    }
}