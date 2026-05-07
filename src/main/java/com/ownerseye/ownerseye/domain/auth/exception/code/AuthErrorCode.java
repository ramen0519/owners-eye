package com.ownerseye.ownerseye.domain.auth.exception.code;

import com.ownerseye.ownerseye.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH-001", "이미 사용 중인 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH-002", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-003", "유효하지 않은 리프레시 토큰입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
