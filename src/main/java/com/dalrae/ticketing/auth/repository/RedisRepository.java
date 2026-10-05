package com.dalrae.ticketing.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private static final String PREFIX = "refresh: ";
    private final StringRedisTemplate stringRedisTemplate;

    public void save(UUID userId, String refreshToken, Duration ttl) {
        stringRedisTemplate.opsForValue().set(PREFIX + userId, refreshToken, ttl);
    }

    public Optional<String> find(UUID userId) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(PREFIX + userId));
    }

    public void delete(UUID userId) {
        stringRedisTemplate.delete(PREFIX + userId);
    }
}
