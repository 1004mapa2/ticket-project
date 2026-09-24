package com.dalrae.ticketing.user.controller;

import com.dalrae.ticketing.user.dto.SignUpRequest;
import com.dalrae.ticketing.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
