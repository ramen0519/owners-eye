package com.ownerseye.ownerseye.domain.analysis.application.dto.response;

import java.util.List;

public record AnalysisResponse(
        String yearMonth,
        long totalRevenue,
        List<ChannelAnalysisResponse> channels
) {
}
