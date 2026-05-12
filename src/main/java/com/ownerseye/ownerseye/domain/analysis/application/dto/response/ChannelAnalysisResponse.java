package com.ownerseye.ownerseye.domain.analysis.application.dto.response;

import java.util.List;

public record ChannelAnalysisResponse(
        String channel,
        long revenue,
        double revenueRatio,
        List<CostItemResponse> costs
) {
    public static ChannelAnalysisResponse of(String channel, long revenue, long totalRevenue,
                                              List<CostItemResponse> costs) {
        double revenueRatio = totalRevenue == 0 ? 0.0
                : Math.round((double) revenue / totalRevenue * 1000) / 10.0;
        return new ChannelAnalysisResponse(channel, revenue, revenueRatio, costs);
    }
}
