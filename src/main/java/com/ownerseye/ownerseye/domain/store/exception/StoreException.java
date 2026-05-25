package com.ownerseye.ownerseye.domain.store.exception;

import com.ownerseye.ownerseye.domain.store.exception.code.StoreErrorCode;
import com.ownerseye.ownerseye.global.exception.AppException;

public class StoreException extends AppException {

    public StoreException(StoreErrorCode errorCode) {
        super(errorCode);
    }
}
