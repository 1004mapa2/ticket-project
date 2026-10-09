package com.dalrae.ticketing.user.service;

import com.dalrae.ticketing.global.Role;
import com.dalrae.ticketing.global.exception.BusinessException;
import com.dalrae.ticketing.global.exception.ErrorCode;
import com.dalrae.ticketing.user.domain.User;
import com.dalrae.ticketing.user.dto.SignUpRequest;
import com.dalrae.ticketing.user.dto.UserResponse;
import com.dalrae.ticketing.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@DisplayName("UserService")
class UserServiceTest {

    private static final String EMAIL = "test@dalrae.com";
    private static final String RAW_PASSWORD = "a1234";
    private static final String NAME = "park";
    private static final String PHONE = "01011112222";

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private final UserService userService = new UserService(userRepository, passwordEncoder);

    @Nested
    @DisplayName("회원가입")
    class SaveUser {
        @BeforeEach
        void before() {
            given(userRepository.existsByEmail(EMAIL)).willReturn(false);
        }

        @Test
        @DisplayName("이메일이 중복되면 DUPLICATE_EMAIL 예외가 발생하고 저장하지 않는다.")
        void duplicateEmail() {
            given(userRepository.existsByEmail(EMAIL)).willReturn(true);

            BusinessException e = assertThrows(BusinessException.class,
                    () -> userService.saveUser(signUpRequest()));

            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL);
            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("비밀번호를 평문이 아닌 해시로 저장한다.")
        void storesHashedPassword() {
            userService.saveUser(signUpRequest());

            User saved = savedUser();
            assertThat(saved.getPassword()).isNotEqualTo(RAW_PASSWORD);
            assertThat(passwordEncoder.matches(RAW_PASSWORD, saved.getPassword())).isTrue();
        }

        @Test
        @DisplayName("요청한 회원 정보와 USER 권한으로 저장한다.")
        void storesUserInfoWithUserRole() {
            userService.saveUser(signUpRequest());

            User saved = savedUser();
            assertThat(saved.getEmail()).isEqualTo(EMAIL);
            assertThat(saved.getName()).isEqualTo(NAME);
            assertThat(saved.getPhone()).isEqualTo(PHONE);
            assertThat(saved.getRole()).isEqualTo(Role.USER);
        }

        private static SignUpRequest signUpRequest() {
            return new SignUpRequest(EMAIL, RAW_PASSWORD, NAME, PHONE);
        }

        private User savedUser() {
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            then(userRepository).should().save(captor.capture());
            return captor.getValue();
        }
    }

    @Nested
    @DisplayName("내 정보 조회")
    class GetMe {
        private final UUID userId = UUID.randomUUID();

        @Test
        @DisplayName("회원 정보를 반환한다.")
        void returnsUserInfo() {
            given(userRepository.findById(userId)).willReturn(Optional.of(existingUser(userId)));

            UserResponse response = userService.getMe(userId);

            assertThat(response.userId()).isEqualTo(userId);
            assertThat(response.email()).isEqualTo(EMAIL);
            assertThat(response.name()).isEqualTo(NAME);
            assertThat(response.phone()).isEqualTo(PHONE);
            assertThat(response.role()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("존재하지 않는 회원이면 USER_NOT_FOUND 예외가 발생한다.")
        void unknownUser() {
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            BusinessException e = assertThrows(BusinessException.class,
                    () -> userService.getMe(userId));

            assertThat(e.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        private User existingUser(UUID userId) {
            User user = User.builder()
                    .email(EMAIL)
                    .password(passwordEncoder.encode(RAW_PASSWORD))
                    .name(NAME)
                    .phone(PHONE)
                    .build();
            ReflectionTestUtils.setField(user, "userId", userId);
            return user;
        }
    }
}