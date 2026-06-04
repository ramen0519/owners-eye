package com.ownerseye.ownerseye.domain.menu_sale.exception.code;

import com.ownerseye.ownerseye.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MenuSaleErrorCode implements BaseErrorCode {

    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "MENU-SALE-001", "가게 정보를 찾을 수 없습니다."),
    STORE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MENU-SALE-002", "해당 가게에 대한 접근 권한이 없습니다."),
    AI_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MENU-SALE-003", "AI 메뉴 판매 분석에 실패했습니다."),
    EXCEL_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MENU-SALE-004", "엑셀 파일 읽기에 실패했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
