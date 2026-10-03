package com.dalrae.ticketing.user.dto;

import com.dalrae.ticketing.global.Role;
import com.dalrae.ticketing.user.domain.User;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
        UUID userId,
        String email,
        String name,
        String phone,
        Role role,
        OffsetDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
