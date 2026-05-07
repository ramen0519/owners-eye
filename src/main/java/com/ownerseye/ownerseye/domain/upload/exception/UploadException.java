package com.ownerseye.ownerseye.domain.upload.exception;

import com.ownerseye.ownerseye.global.exception.AppException;
import com.ownerseye.ownerseye.global.exception.BaseErrorCode;

public class UploadException extends AppException {
    public UploadException(BaseErrorCode code) {
        super(code);
    }
}
