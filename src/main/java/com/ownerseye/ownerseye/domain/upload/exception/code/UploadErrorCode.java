package com.ownerseye.ownerseye.domain.upload.exception.code;

import com.ownerseye.ownerseye.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UploadErrorCode implements BaseErrorCode {

    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "UPLOAD-001", "등록된 가게를 찾을 수 없습니다."),
    PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "UPLOAD-002", "엑셀 파싱에 실패했습니다."),
    INVALID_FILE(HttpStatus.BAD_REQUEST, "UPLOAD-003", "유효하지 않은 파일입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
