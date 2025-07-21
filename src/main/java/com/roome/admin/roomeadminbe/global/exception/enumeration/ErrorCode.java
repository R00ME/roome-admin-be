package com.roome.admin.roomeadminbe.global.exception.enumeration;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    UNHANDLED_EXCEPTION("알 수 없는 오류입니다.", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("사용자 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND);

    private final String message;
    private final HttpStatus status;

    ErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
