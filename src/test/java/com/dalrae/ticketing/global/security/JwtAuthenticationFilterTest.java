package com.dalrae.ticketing.global.security;

import com.dalrae.ticketing.global.Role;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    private static final String SECRET_KEY = "wkwP5PsUMjeA8PqoMe6PzotiSAgWanskHWNjtL1AYmU=";
    private static final Duration ACCESS_VALIDITY = Duration.ofMinutes(30);
    private static final Duration REFRESH_VALIDITY = Duration.ofDays(7);
    private final JwtProvider jwtProvider = new JwtProvider(new JwtProperties(SECRET_KEY, ACCESS_VALIDITY, REFRESH_VALIDITY));
    private final JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtProvider);
    private final UUID userId = UUID.randomUUID();
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void before() {
        SecurityContextHolder.clearContext();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @Test
    @DisplayName("유효한 액세스 토큰이면 SecurityContext에 userId와 권한이 저장된다.")
    void givenValidAccessToken_whenFilter_thenAuthenticationIsSet() throws Exception {
        String accessToken = jwtProvider.createAccessToken(userId, Role.USER);

        doFilterWithAuthorization("Bearer " + accessToken);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_USER");
        assertThat(filterChain.getRequest()).isNotNull();

    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 인증 정보 없이 다음 필터로 넘어간다.")
    void givenNoAuthorizationHeader_whenFilter_thenPassWithoutAuthentication() throws Exception {

        doFilterWithAuthorization(null);

        assertNotAuthenticatedAndPassed();

    }

    @Test
    @DisplayName("Bearer 접두사가 없으면 인증 정보 없이 다음 필터로 넘어간다.")
    void givenTokenWithoutBearerPrefix_whenFilter_thenPassWithoutAuthentication() throws Exception {
        String accessToken = jwtProvider.createAccessToken(userId, Role.USER);

        doFilterWithAuthorization(accessToken);

        assertNotAuthenticatedAndPassed();
    }

    @Test
    @DisplayName("변조된 토큰이면 인증 정보 없이 다음 필터로 넘어간다.")
    void givenTamperedToken_whenFilter_thenPassWithoutAuthentication() throws Exception {
        String accessToken = jwtProvider.createAccessToken(userId, Role.USER);
        String authorization = "Bearer " + accessToken.substring(0, accessToken.length() - 2) + "xx";

        doFilterWithAuthorization(authorization);

        assertNotAuthenticatedAndPassed();
    }

    @Test
    @DisplayName("만료된 토큰이면 인증 정보 없이 다음 필터로 넘어간다.")
    void givenExpiredToken_whenFilter_thenPassWithoutAuthentication() throws Exception {
        String expiredAccessToken = new JwtProvider(new JwtProperties(SECRET_KEY, Duration.ofMinutes(-1), REFRESH_VALIDITY)).createAccessToken(userId, Role.USER);

        doFilterWithAuthorization("Bearer " + expiredAccessToken);

        assertNotAuthenticatedAndPassed();

    }

    @Test
    @DisplayName("리프레시 토큰이면 인증 정보 없이 다음 필터로 넘어간다.")
    void givenRefreshToken_whenFilter_thenPassWithoutAuthentication() throws Exception {
        String refreshToken = jwtProvider.createRefreshToken(userId);

        doFilterWithAuthorization("Bearer " + refreshToken);

        assertNotAuthenticatedAndPassed();
    }

    private void doFilterWithAuthorization(String authorization) throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (authorization != null) {
            request.addHeader(HttpHeaders.AUTHORIZATION, authorization);
        }
        jwtAuthenticationFilter.doFilter(request, response, filterChain);
    }

    private void assertNotAuthenticatedAndPassed() {
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(filterChain.getRequest()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsByteArray()).isEmpty();
    }
}