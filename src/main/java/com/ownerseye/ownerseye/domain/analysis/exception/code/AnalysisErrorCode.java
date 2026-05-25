package com.ownerseye.ownerseye.domain.analysis.exception.code;

import com.ownerseye.ownerseye.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AnalysisErrorCode implements BaseErrorCode {

    STORE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ANALYSIS-001", "해당 가게에 대한 접근 권한이 없습니다."),
    INVALID_YEAR_MONTH_FORMAT(HttpStatus.BAD_REQUEST, "ANALYSIS-002", "연월 형식이 올바르지 않습니다. (형식: yyyy-MM)"),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
