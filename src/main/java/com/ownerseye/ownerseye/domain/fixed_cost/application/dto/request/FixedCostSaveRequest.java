package com.ownerseye.ownerseye.domain.fixed_cost.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FixedCostSaveRequest(
        @NotNull Long storeId,
        @NotBlank String yearMonth,
        Long materialCost,
        Long storeDeliveryFee,
        Long laborCost,
        Long utilities,
        Long rent,
        Long consumables,
        Long other
) {
}
