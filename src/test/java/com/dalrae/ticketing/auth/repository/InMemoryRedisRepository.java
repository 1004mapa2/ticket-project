package com.dalrae.ticketing.auth.repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryRedisRepository extends RedisRepository {

    private final Map<UUID, String> store = new HashMap<>();

    public InMemoryRedisRepository() {
        super(null);
    }

    @Override
    public void save(UUID userId, String refreshToken, Duration ttl) {
        store.put(userId, refreshToken);
    }

    @Override
    public Optional<String> find(UUID userId) {
        return Optional.ofNullable(store.get(userId));
    }

    @Override
    public void delete(UUID userId) {
        store.remove(userId);
    }
}
