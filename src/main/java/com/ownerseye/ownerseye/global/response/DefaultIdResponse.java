package com.ownerseye.ownerseye.global.response;

public record DefaultIdResponse(
        Long id
) {
    public static DefaultIdResponse of(Long id) {
        return new DefaultIdResponse(id);
    }
}
