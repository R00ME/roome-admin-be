package com.roome.admin.roomeadminbe.domain.common.dto.response;

import com.roome.admin.roomeadminbe.global.exception.enumeration.ErrorCode;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@Builder
public class CommonResponse<T> {

    boolean success;
    T data;
    String code;
    String message;

    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> CommonResponse<T> error(ErrorCode errorCode){
        return CommonResponse.<T>builder()
                .success(false)
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .build();
    }
    public static <T> ResponseEntity<CommonResponse<T>> ofDataWithHttpStatus(T data, HttpStatus httpStatus) {
        return ResponseEntity.status(httpStatus).body(success(data));
    }

    public static <T> ResponseEntity<CommonResponse<T>> ofError(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(error(errorCode));
    }
}
