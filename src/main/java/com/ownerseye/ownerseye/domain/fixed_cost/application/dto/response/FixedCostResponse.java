package com.ownerseye.ownerseye.domain.fixed_cost.application.dto.response;

import com.ownerseye.ownerseye.domain.fixed_cost.persistence.entity.FixedCostEntity;

import java.time.format.DateTimeFormatter;

public record FixedCostResponse(
        Long fixedCostId,
        Long storeId,
        String yearMonth,
        long materialCost,
        long storeDeliveryFee,
        long laborCost,
        long utilities,
        long rent,
        long consumables,
        long other
) {
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public static FixedCostResponse from(FixedCostEntity entity) {
        return new FixedCostResponse(
                entity.getFixedCostId(),
                entity.getStoreId(),
                entity.getYearMonth().format(YEAR_MONTH_FORMATTER),
                entity.getMaterialCost(),
                entity.getStoreDeliveryFee(),
                entity.getLaborCost(),
                entity.getUtilities(),
                entity.getRent(),
                entity.getConsumables(),
                entity.getOther()
        );
    }
}
