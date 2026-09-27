package com.dalrae.ticketing.global.exception;

import lombok.Getter;

@Getter
public final class ErrorResponse {
    private final int status;
    private final String code;
    private final String message;

    private ErrorResponse(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return of(errorCode, errorCode.getMessage());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.getStatus().value(), errorCode.getCode(), message);
    }
}
