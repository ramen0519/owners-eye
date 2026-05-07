package com.ownerseye.ownerseye.domain.auth.exception;

import com.ownerseye.ownerseye.global.exception.AppException;
import com.ownerseye.ownerseye.global.exception.BaseErrorCode;

public class AuthException extends AppException {
    public AuthException(BaseErrorCode code) {
        super(code);
    }
}
