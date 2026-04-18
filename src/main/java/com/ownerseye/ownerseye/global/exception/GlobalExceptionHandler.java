package com.ownerseye.ownerseye.global.exception;

import com.ownerseye.ownerseye.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllException(Exception e, HttpServletRequest request) {
        log.error("[ERROR] event_type=unhandled_exception method={} uri={} exception={}",
                request.getMethod(), request.getRequestURI(), e.getClass().getSimpleName(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, request));
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException e, HttpServletRequest request) {
        BaseErrorCode errorCode = e.getErrorCode();
        if (errorCode.getHttpStatus().is5xxServerError()) {
            log.error("[ERROR] event_type=app_exception error_code={} method={} uri={}",
                    errorCode.getCode(), request.getMethod(), request.getRequestURI());
        } else {
            log.warn("[WARN] event_type=app_exception error_code={} method={} uri={}",
                    errorCode.getCode(), request.getMethod(), request.getRequestURI());
        }
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode, request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        String field = e.getBindingResult().getFieldErrors().isEmpty()
                ? "unknown"
                : e.getBindingResult().getFieldErrors().get(0).getField();
        log.warn("[WARN] event_type=validation_exception field={} method={} uri={}",
                field, request.getMethod(), request.getRequestURI());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.INVALID_PARAMETER, request));
    }
}
