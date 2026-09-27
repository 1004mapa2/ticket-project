package com.dalrae.ticketing.user.service;

import com.dalrae.ticketing.global.exception.BusinessException;
import com.dalrae.ticketing.global.exception.ErrorCode;
import com.dalrae.ticketing.user.dto.SignUpRequest;
import com.dalrae.ticketing.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void validateDuplicateEmail() {
        // given
        SignUpRequest request = new SignUpRequest("test@dalrae.com", "a1234", "park", "01011112222");
        given(userRepository.existsByEmail("test@dalrae.com")).willReturn(true);

        // when, then
        assertThatThrownBy(() -> userService.saveUser(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);

        verify(userRepository, never()).save(any());
    }

}