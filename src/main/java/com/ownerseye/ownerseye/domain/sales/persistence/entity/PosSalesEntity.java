package com.ownerseye.ownerseye.domain.sales.persistence.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PosSalesEntity {

    private Long posSaleId;
    private Long uploadId;
    private LocalDate yearMonth;
    private String channel;
    private long totalRevenue;
    private long avgPrice;
    private long totalOrders;

    @Builder
    public PosSalesEntity(Long uploadId, LocalDate yearMonth, String channel,
                          long totalRevenue, long avgPrice, long totalOrders) {
        this.uploadId = uploadId;
        this.yearMonth = yearMonth;
        this.channel = channel;
        this.totalRevenue = totalRevenue;
        this.avgPrice = avgPrice;
        this.totalOrders = totalOrders;
    }
}
