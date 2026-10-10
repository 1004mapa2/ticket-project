package com.dalrae.ticketing.auth.controller;

import com.dalrae.ticketing.auth.dto.LoginRequest;
import com.dalrae.ticketing.auth.dto.ReissueRequest;
import com.dalrae.ticketing.auth.dto.TokenResponse;
import com.dalrae.ticketing.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/reissue")
    public TokenResponse reissue(@Valid @RequestBody ReissueRequest request) {
        return authService.reissue(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal UUID userId) {
        authService.logout(userId);
    }
}
