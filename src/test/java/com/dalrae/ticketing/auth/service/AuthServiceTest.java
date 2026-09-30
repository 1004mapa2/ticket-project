package com.dalrae.ticketing.auth.service;

import com.dalrae.ticketing.auth.dto.LoginRequest;
import com.dalrae.ticketing.auth.dto.TokenResponse;
import com.dalrae.ticketing.global.Role;
import com.dalrae.ticketing.global.exception.BusinessException;
import com.dalrae.ticketing.global.exception.ErrorCode;
import com.dalrae.ticketing.global.security.JwtProvider;
import com.dalrae.ticketing.user.domain.User;
import com.dalrae.ticketing.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    private static final String EMAIL = "test@dalrae.com";
    private static final String RAW_PASSWORD = "a1234";
    private static final String ENCODED_PASSWORD = "encoded-password";

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("이메일과 비밀번호가 일치하면 AccessToken을 발급한다.")
    void givenValidCredentials_whenLogin_thenReturnsAccessToken() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(createUser(userId)));
        given(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).willReturn(true);
        given(jwtProvider.createAccessToken(userId, Role.USER)).willReturn("access-token");

        TokenResponse response = authService.login(new LoginRequest(EMAIL, RAW_PASSWORD));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isNull();
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("존재하지 않는 이메일이면 예외를 발생시키고 토큰을 발급하지 않는다.")
    void givenUnknownEmail_whenLogin_thenThrowsLoginFailed() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, RAW_PASSWORD)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LOGIN_FAILED);

        verify(passwordEncoder, never()).matches(any(), anyString());
        verify(jwtProvider, never()).createAccessToken(any(), any());
    }

    @Test
    @DisplayName("비밀번호가 틀리면 예외를 발생시키고 토큰을 발급하지 않는다.")
    void givenWrongPassword_whenLogin_thenThrowsLoginFailed() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(createUser(UUID.randomUUID())));
        given(passwordEncoder.matches("wrong-password", ENCODED_PASSWORD)).willReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, "wrong-password")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LOGIN_FAILED);

        verify(jwtProvider, never()).createAccessToken(any(), any());
    }

    private User createUser(UUID userId) {
        User user = User.builder()
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .name("park")
                .phone("01011112222")
                .build();
        ReflectionTestUtils.setField(user, "userId", userId);
        return user;
    }
}