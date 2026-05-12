package com.ownerseye.ownerseye.domain.analysis.exception;

import com.ownerseye.ownerseye.domain.analysis.exception.code.AnalysisErrorCode;
import com.ownerseye.ownerseye.global.exception.AppException;

public class AnalysisException extends AppException {

    public AnalysisException(AnalysisErrorCode errorCode) {
        super(errorCode);
    }
}
