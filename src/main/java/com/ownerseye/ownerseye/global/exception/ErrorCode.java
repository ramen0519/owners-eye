package com.ownerseye.ownerseye.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode implements BaseErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON-001", "잘못된 요청입니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON-002", "요청 파라미터가 잘못되었습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-003", "찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-004", "서버 내부에서 에러가 발생하였습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
