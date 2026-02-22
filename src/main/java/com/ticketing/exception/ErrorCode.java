package com.ticketing.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 비즈니스 예외 에러 코드 정의.
 * 각 에러 코드는 HTTP 상태 코드와 사용자에게 보여줄 메시지를 포함.
 * 일부 메시지는 포맷 문자열(%s)을 포함하여 동적으로 값을 전달할 수 있음.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ========== 예약 관련 ==========
    INSUFFICIENT_INVENTORY(HttpStatus.BAD_REQUEST, "요청하신 수량이 재고보다 많습니다."),
    PURCHASE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "1일 구매 한도를 초과했습니다."),
    RESERVATION_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "현재 예약할 수 없는 디저트입니다."),

    // ========== 리소스 관련 ==========
    NOT_FOUND(HttpStatus.NOT_FOUND, "%s를 찾을 수 없습니다."),  // 동적 메시지

    // ========== 인증/인가 관련 ==========
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // ========== 서버 오류 ==========
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
