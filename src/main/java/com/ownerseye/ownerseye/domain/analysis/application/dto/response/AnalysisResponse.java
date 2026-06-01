package com.ownerseye.ownerseye.domain.analysis.application.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AnalysisResponse(
        String yearMonth,
        long totalRevenue,
        List<ChannelAnalysisResponse> channels
) {
    @JsonCreator
    public AnalysisResponse(
            @JsonProperty("yearMonth") String yearMonth,
            @JsonProperty("totalRevenue") long totalRevenue,
            @JsonProperty("channels") List<ChannelAnalysisResponse> channels
    ) {
        this.yearMonth = yearMonth;
        this.totalRevenue = totalRevenue;
        this.channels = channels;
    }
}
