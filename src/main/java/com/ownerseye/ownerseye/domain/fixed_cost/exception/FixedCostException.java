package com.ownerseye.ownerseye.domain.fixed_cost.exception;

import com.ownerseye.ownerseye.global.exception.AppException;
import com.ownerseye.ownerseye.global.exception.BaseErrorCode;

public class FixedCostException extends AppException {

    public FixedCostException(BaseErrorCode code) {
        super(code);
    }
}
