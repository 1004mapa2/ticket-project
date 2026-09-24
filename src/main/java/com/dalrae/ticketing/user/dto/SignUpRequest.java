package com.dalrae.ticketing.user.dto;

import com.dalrae.ticketing.user.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
        @NotBlank @Email
        String email,
        @NotBlank
        String password,
        String name,
        String phone
) {
    public User toEntity(String encodedPassword) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)
                .phone(phone)
                .build();
    }
}
