package com.roome.admin.roomeadminbe.global.exception.handler;

import com.roome.admin.roomeadminbe.domain.common.dto.response.CommonResponse;
import com.roome.admin.roomeadminbe.global.exception.BusinessException;
import com.roome.admin.roomeadminbe.global.exception.enumeration.ErrorCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public CommonResponse<Object> handleCustomException(BusinessException e) {
        return CommonResponse.error(e.getErrorCode());
    }

    @ExceptionHandler(Exception.class)
    public CommonResponse<Object> handleUnexpectedException(Exception e) {
        ErrorCode errorCode = ErrorCode.UNHANDLED_EXCEPTION;
        return CommonResponse.error(errorCode);
    }
}
