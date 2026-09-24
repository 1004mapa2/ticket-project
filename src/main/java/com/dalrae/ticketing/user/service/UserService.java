package com.dalrae.ticketing.user.service;

import com.dalrae.ticketing.user.domain.User;
import com.dalrae.ticketing.user.dto.SignUpRequest;
import com.dalrae.ticketing.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void saveUser(SignUpRequest request) {
        if(userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = userRepository.save(request.toEntity(encodedPassword));
        log.info("user {}", user);

        userRepository.save(user);
    }
}
