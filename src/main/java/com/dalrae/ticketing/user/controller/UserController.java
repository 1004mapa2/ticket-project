package com.dalrae.ticketing.user.controller;

import com.dalrae.ticketing.user.dto.SignUpRequest;
import com.dalrae.ticketing.user.dto.UserResponse;
import com.dalrae.ticketing.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public String signUp(@Valid @RequestBody SignUpRequest request) {
        userService.saveUser(request);
        return "ok";
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UUID userId) {
        return userService.getMe(userId);
    }
}
