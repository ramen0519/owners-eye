package com.ownerseye.ownerseye.domain.sales.domain.constant;

import java.util.Arrays;
import java.util.Optional;

public enum PosChannel {
    PHONE("전화"),
    DDANGYEO("땡겨요"),
    YOGIYO("요기요"),
    DELIVERY_SPECIAL("배달특급"),
    ETC("기타");

    private final String korName;

    PosChannel(String korName) {
        this.korName = korName;
    }

    public static Optional<PosChannel> fromKorName(String korName) {
        return Arrays.stream(values())
                .filter(c -> c.korName.equals(korName))
                .findFirst();
    }
}
