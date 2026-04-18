package com.ownerseye.ownerseye.domain.example.exception.code;

import com.ownerseye.ownerseye.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExampleErrorCode implements BaseErrorCode {

    EXAMPLE_NOT_FOUND(HttpStatus.NOT_FOUND, "EXAMPLE-001", "엔티티를 찾을 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
