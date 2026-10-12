package com.dalrae.ticketing.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다."),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "C002", "요청 본문이 없거나 형식이 올바르지 않습니다."),
    INVALID_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "C003", "요청 파라미터의 형식이 올바르지 않습니다."),

    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "U001", "이미 사용 중인 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U002", "존재하지 않는 회원입니다."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "권한이 없습니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "A003", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "유효하지 않은 리프레시 토큰입니다."),

    INVALID_SCHEDULE_TIME(HttpStatus.BAD_REQUEST, "T001", "예매 오픈 시각은 공연 일시보다 이전이어야 합니다."),
    CONCERT_NOT_FOUND(HttpStatus.NOT_FOUND, "T002", "존재하지 않는 공연입니다."),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "T003", "존재하지 않는 회차입니다."),
    DUPLICATE_SCHEDULE(HttpStatus.CONFLICT, "T004", "같은 일시의 회차가 이미 존재합니다."),
    INVALID_SEAT_RANGE(HttpStatus.BAD_REQUEST, "T005", "좌석 범위가 올바르지 않습니다."),
    DUPLICATE_SEAT(HttpStatus.CONFLICT, "T006", "이미 존재하는 좌석이 포함되어 있습니다."),
    SEAT_CREATION_CLOSED(HttpStatus.CONFLICT, "T007", "예매가 오픈된 회차에는 좌석을 생성할 수 없습니다."),
    PAST_PERFORMANCE_TIME(HttpStatus.BAD_REQUEST, "T008", "공연 일시는 현재 이후여야 합니다."),

    VENUE_NOT_FOUND(HttpStatus.NOT_FOUND, "V001", "존재하지 않는 공연장입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
