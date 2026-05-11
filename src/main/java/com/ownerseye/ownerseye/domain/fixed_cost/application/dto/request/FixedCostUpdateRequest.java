package com.ownerseye.ownerseye.domain.fixed_cost.application.dto.request;

/**
 * 고정비용 수정 요청 DTO
 *
 * @param materialCost     재료비
 * @param storeDeliveryFee 가게배달료
 * @param laborCost        인건비
 * @param utilities        수도가스전기요금
 * @param rent             임대료
 * @param consumables      소모품
 * @param other            기타
 */
public record FixedCostUpdateRequest(
        Long materialCost,
        Long storeDeliveryFee,
        Long laborCost,
        Long utilities,
        Long rent,
        Long consumables,
        Long other
) {
}
