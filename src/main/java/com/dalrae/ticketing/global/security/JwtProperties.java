package com.dalrae.ticketing.global.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "jwt")
public final class JwtProperties {

    private final String secret;
    private final Duration accessTokenValidity;
    private final Duration refreshTokenValidity;

}
