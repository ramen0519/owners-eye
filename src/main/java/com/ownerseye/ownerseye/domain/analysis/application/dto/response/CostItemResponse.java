package com.ownerseye.ownerseye.domain.analysis.application.dto.response;

public record CostItemResponse(
        String name,
        long amount,
        double ratio
) {
    public static CostItemResponse of(String name, long amount, long revenue) {
        double ratio = revenue == 0 ? 0.0 : Math.round((double) amount / revenue * 1000) / 10.0;
        return new CostItemResponse(name, amount, ratio);
    }
}
