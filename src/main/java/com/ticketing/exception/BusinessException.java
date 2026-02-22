package com.ticketing.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 비즈니스 로직 예외를 나타내는 기본 예외 클래스.
 * ErrorCode를 통해 HTTP 상태 코드와 메시지를 관리.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * ErrorCode로 예외 생성.
     * ErrorCode의 메시지를 그대로 사용.
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode와 포맷 인자로 예외 생성.
     * ErrorCode의 메시지가 포맷 문자열(%s 등)을 포함할 경우 사용.
     *
     * @param errorCode 에러 코드
     * @param args 메시지 포맷 인자
     *
     * 사용 예시:
     * throw new BusinessException(ErrorCode.NOT_FOUND, "디저트");
     * → "디저트를 찾을 수 없습니다."
     */
    public BusinessException(ErrorCode errorCode, Object... args) {
        super(String.format(errorCode.getMessage(), args));
        this.errorCode = errorCode;
    }

    /**
     * HTTP 상태 코드 반환.
     */
    public HttpStatus getStatus() {
        return errorCode.getStatus();
    }
}
