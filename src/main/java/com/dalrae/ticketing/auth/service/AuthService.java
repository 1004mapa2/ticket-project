package com.dalrae.ticketing.auth.service;

import com.dalrae.ticketing.auth.dto.LoginRequest;
import com.dalrae.ticketing.auth.dto.ReissueRequest;
import com.dalrae.ticketing.auth.dto.TokenResponse;
import com.dalrae.ticketing.auth.repository.RedisRepository;
import com.dalrae.ticketing.global.exception.BusinessException;
import com.dalrae.ticketing.global.exception.ErrorCode;
import com.dalrae.ticketing.global.security.JwtProvider;
import com.dalrae.ticketing.user.domain.User;
import com.dalrae.ticketing.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.dalrae.ticketing.global.exception.ErrorCode.INVALID_REFRESH_TOKEN;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RedisRepository redisRepository;

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        return issueTokens(user);
    }

    public TokenResponse reissue(ReissueRequest request) {
        UUID userId = extractUserId(request.refreshToken());
        String redisSavedToken = redisRepository.find(userId).orElseThrow(() -> new BusinessException(INVALID_REFRESH_TOKEN));
        if (!redisSavedToken.equals(request.refreshToken())) {
            redisRepository.delete(userId);
            throw new BusinessException(INVALID_REFRESH_TOKEN);
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(INVALID_REFRESH_TOKEN));
        return issueTokens(user);
    }

    private UUID extractUserId(String refreshToken) {
        try {
            Claims claims = jwtProvider.parseRefreshToken(refreshToken);
            return UUID.fromString(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(INVALID_REFRESH_TOKEN);
        }
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId());
        redisRepository.save(user.getUserId(), refreshToken, jwtProvider.getRefreshTokenValidity());
        return TokenResponse.of(accessToken, refreshToken);
    }
}
