package com.ownerseye.ownerseye.domain.example.exception;

import com.ownerseye.ownerseye.global.exception.AppException;
import com.ownerseye.ownerseye.global.exception.BaseErrorCode;

public class ExampleException extends AppException {
    public ExampleException(BaseErrorCode code) {
        super(code);
    }
}
