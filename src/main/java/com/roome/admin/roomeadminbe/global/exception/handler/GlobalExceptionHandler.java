package com.roome.admin.roomeadminbe.global.exception.handler;

import com.roome.admin.roomeadminbe.domain.common.dto.response.CommonResponse;
import com.roome.admin.roomeadminbe.global.exception.BusinessException;
import com.roome.admin.roomeadminbe.global.exception.enumeration.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.context.support.DefaultMessageSourceResolvable;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public CommonResponse<Object> handleCustomException(BusinessException e) {
        return CommonResponse.error(e.getErrorCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Object>> handleAll(Exception ex) {
        log.error("처리되지 않은 예외 발생", ex);
        return CommonResponse.ofError(ErrorCode.UNHANDLED_EXCEPTION);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CommonResponse<Object>> handleUnexpectedException(RuntimeException e) {
        ErrorCode errorCode = ErrorCode.UNHANDLED_EXCEPTION;
        return CommonResponse.ofError(errorCode);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", errorMessage);
        return CommonResponse.ofError(ErrorCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonResponse<Object>> handleJsonParseError(HttpMessageNotReadableException ex) {
        log.warn("JSON 파싱 에러: {}", ex.getMessage());
        return CommonResponse.ofError(ErrorCode.INVALID_JSON);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<CommonResponse<Object>> handleBindException(BindException ex) {
        String message = ex.getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("바인딩 실패: {}", message);
        return CommonResponse.ofError(ErrorCode.INVALID_INPUT_VALUE);
    }
}
