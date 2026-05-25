package com.ownerseye.ownerseye.domain.fixed_cost.persistence.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FixedCostEntity {

    private Long fixedCostId;
    private Long storeId;
    private LocalDate yearMonth;
    private long materialCost;       // 재료비
    private long storeDeliveryFee;   // 가게배달료
    private long laborCost;          // 인건비
    private long utilities;          // 수도가스전기요금
    private long rent;               // 임대료
    private long consumables;        // 소모품
    private long other;              // 기타
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public FixedCostEntity(Long storeId, LocalDate yearMonth,
                           long materialCost, long storeDeliveryFee, long laborCost,
                           long utilities, long rent, long consumables, long other) {
        this.storeId = storeId;
        this.yearMonth = yearMonth;
        this.materialCost = materialCost;
        this.storeDeliveryFee = storeDeliveryFee;
        this.laborCost = laborCost;
        this.utilities = utilities;
        this.rent = rent;
        this.consumables = consumables;
        this.other = other;
    }
}
