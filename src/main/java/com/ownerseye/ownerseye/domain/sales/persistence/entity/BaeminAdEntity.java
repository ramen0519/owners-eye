package com.ownerseye.ownerseye.domain.sales.persistence.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaeminAdEntity {

    private Long baeminAdId;
    private Long uploadId;
    private LocalDate yearMonth;
    private long adFee;

    @Builder
    public BaeminAdEntity(Long uploadId, LocalDate yearMonth, long adFee) {
        this.uploadId = uploadId;
        this.yearMonth = yearMonth;
        this.adFee = adFee;
    }
}
