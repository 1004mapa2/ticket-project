package com.dalrae.ticketing.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public final class ErrorResponse {
    private final int status;
    private final String message;

    private ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public static ErrorResponse of(HttpStatus status, String message) {

        return new ErrorResponse(status.value(), message);
    }
}
