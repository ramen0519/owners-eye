package com.ownerseye.ownerseye.domain.store.exception.code;

import com.ownerseye.ownerseye.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StoreErrorCode implements BaseErrorCode {

    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE-001", "가게 정보를 찾을 수 없습니다."),
    STORE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "STORE-002", "해당 가게에 대한 접근 권한이 없습니다."),
    STORE_ALREADY_EXISTS(HttpStatus.CONFLICT, "STORE-003", "이미 동일한 이름의 가게가 존재합니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
