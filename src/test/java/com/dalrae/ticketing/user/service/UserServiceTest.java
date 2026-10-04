package com.dalrae.ticketing.user.service;

import com.dalrae.ticketing.global.Role;
import com.dalrae.ticketing.global.exception.BusinessException;
import com.dalrae.ticketing.global.exception.ErrorCode;
import com.dalrae.ticketing.user.domain.User;
import com.dalrae.ticketing.user.dto.SignUpRequest;
import com.dalrae.ticketing.user.dto.UserResponse;
import com.dalrae.ticketing.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("이메일이 중복되면 예외가 발생하고 저장하지 않는다.")
    void givenDuplicateEmail_whenSaveUser_thenThrowsBusinessExceptionAndDoesNotSave() {
        SignUpRequest request = new SignUpRequest("test@dalrae.com", "a1234", "park", "01011112222");
        given(userRepository.existsByEmail("test@dalrae.com")).willReturn(true);

        assertThatThrownBy(() -> userService.saveUser(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원 ID로 조회하면 비밀번호를 제외한 회원 정보를 반환한다.")
    void givenExistUserId_whenGetMe_thenReturnUserResponse() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findById(userId)).willReturn(Optional.of(createUser(userId)));

        UserResponse response = userService.getMe(userId);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("test@dalrae.com");
        assertThat(response.name()).isEqualTo("park");
        assertThat(response.phone()).isEqualTo("01011112222");
        assertThat(response.role()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 조회하면 USER_NOT_FOUND 예외가 발생한다.")
    void givenUnknownUserId_whenGetMe_thenThrowsUserNotFound() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMe(userId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    private static User createUser(UUID userId) {
        User user = User.builder()
                .email("test@dalrae.com")
                .password("a1234")
                .name("park")
                .phone("01011112222")
                .build();
        ReflectionTestUtils.setField(user, "userId", userId);
        return user;
    }

}