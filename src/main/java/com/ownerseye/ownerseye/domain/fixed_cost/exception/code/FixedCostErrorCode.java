package com.ownerseye.ownerseye.domain.fixed_cost.exception.code;

import com.ownerseye.ownerseye.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FixedCostErrorCode implements BaseErrorCode {

    FIXED_COST_NOT_FOUND(HttpStatus.NOT_FOUND, "FIXED-COST-001", "고정비용 정보를 찾을 수 없습니다."),
    FIXED_COST_ALREADY_EXISTS(HttpStatus.CONFLICT, "FIXED-COST-002", "해당 가게의 해당 월 고정비용이 이미 존재합니다."),
    INVALID_YEAR_MONTH_FORMAT(HttpStatus.BAD_REQUEST, "FIXED-COST-003", "연월 형식이 올바르지 않습니다. (형식: yyyy-MM)"),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "FIXED-COST-004", "가게 정보를 찾을 수 없습니다."),
    STORE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "FIXED-COST-005", "해당 가게에 대한 접근 권한이 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
