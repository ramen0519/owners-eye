package com.ownerseye.ownerseye.global.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseResponse {

    private final String status;

    protected BaseResponse(HttpStatus status) {
        this.status = status.getReasonPhrase();
    }
}
