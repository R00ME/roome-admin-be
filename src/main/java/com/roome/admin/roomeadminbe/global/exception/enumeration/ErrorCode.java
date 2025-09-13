package com.roome.admin.roomeadminbe.global.exception.enumeration;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    UNHANDLED_EXCEPTION("알 수 없는 오류입니다.", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("사용자 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    EXISTS_ADMIN("이미 존재하는 관리자입니다.", HttpStatus.CONFLICT),
    PASSWORD_NOT_MATCHES("비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_INPUT_VALUE("유효하지 않은 요청값입니다.", HttpStatus.BAD_REQUEST),
    INVALID_JSON("json 파싱에 실패했습니다.", HttpStatus.BAD_REQUEST),
    NOTIFICATION_NOT_FOUND("알림을 찾을 수 없습니다.", HttpStatus.NOT_FOUND ),
    DATABASE_QUERY_FAILED("유효하지 않은 쿼리입니다.", HttpStatus.INTERNAL_SERVER_ERROR),;

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
