package com.ownerseye.ownerseye.domain.analysis.application.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CostItemResponse(
        String name,
        long amount,
        double ratio
) {
    @JsonCreator
    public CostItemResponse(
            @JsonProperty("name") String name,
            @JsonProperty("amount") long amount,
            @JsonProperty("ratio") double ratio
    ) {
        this.name = name;
        this.amount = amount;
        this.ratio = ratio;
    }

    public static CostItemResponse of(String name, long amount, long revenue) {
        double ratio = revenue == 0 ? 0.0 : Math.round((double) amount / revenue * 1000) / 10.0;
        return new CostItemResponse(name, amount, ratio);
    }
}
