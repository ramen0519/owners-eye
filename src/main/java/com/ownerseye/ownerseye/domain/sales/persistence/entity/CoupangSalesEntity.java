package com.ownerseye.ownerseye.domain.sales.persistence.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoupangSalesEntity {

    private Long coupangSaleId;
    private Long uploadId;
    private LocalDate yearMonth;
    private long orderAmount;
    private long serviceFee;
    private long paymentFee;
    private long deliveryFee;
    private long instantDiscount;
    private long couponDiscount;
    private long adFee;

    @Builder
    public CoupangSalesEntity(Long uploadId, LocalDate yearMonth,
                               long orderAmount, long serviceFee, long paymentFee,
                               long deliveryFee, long instantDiscount, long couponDiscount, long adFee) {
        this.uploadId = uploadId;
        this.yearMonth = yearMonth;
        this.orderAmount = orderAmount;
        this.serviceFee = serviceFee;
        this.paymentFee = paymentFee;
        this.deliveryFee = deliveryFee;
        this.instantDiscount = instantDiscount;
        this.couponDiscount = couponDiscount;
        this.adFee = adFee;
    }
}
