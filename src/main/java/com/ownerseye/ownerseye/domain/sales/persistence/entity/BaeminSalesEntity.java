package com.ownerseye.ownerseye.domain.sales.persistence.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaeminSalesEntity {

    private Long baeminDeliveryId;
    private Long uploadId;
    private LocalDate yearMonth;
    private String deliveryType;
    private long orderAmount;
    private long serviceFee;
    private long couponDiscount;
    private long deliveryCost;
    private long paymentFee;
    private long vat;

    @Builder
    public BaeminSalesEntity(Long uploadId, LocalDate yearMonth, String deliveryType,
                              long orderAmount, long serviceFee, long couponDiscount,
                              long deliveryCost, long paymentFee, long vat) {
        this.uploadId = uploadId;
        this.yearMonth = yearMonth;
        this.deliveryType = deliveryType;
        this.orderAmount = orderAmount;
        this.serviceFee = serviceFee;
        this.couponDiscount = couponDiscount;
        this.deliveryCost = deliveryCost;
        this.paymentFee = paymentFee;
        this.vat = vat;
    }
}
