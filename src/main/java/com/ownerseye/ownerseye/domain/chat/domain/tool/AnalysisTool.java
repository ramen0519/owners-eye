package com.ownerseye.ownerseye.domain.chat.domain.tool;

import com.ownerseye.ownerseye.domain.analysis.application.dto.response.AnalysisResponse;
import com.ownerseye.ownerseye.domain.analysis.application.dto.response.ChannelAnalysisResponse;
import com.ownerseye.ownerseye.domain.analysis.application.dto.response.CostItemResponse;
import com.ownerseye.ownerseye.domain.analysis.application.service.AnalysisService;
import org.springframework.ai.tool.annotation.Tool;

public class AnalysisTool {

    private final AnalysisService analysisService;
    private final Long userId;
    private final Long storeId;

    public AnalysisTool(AnalysisService analysisService, Long userId, Long storeId) {
        this.analysisService = analysisService;
        this.userId = userId;
        this.storeId = storeId;
    }

    @Tool(description = "특정 연월의 가게 매출을 분석합니다. yearMonth는 'yyyy-MM' 형식입니다. 예: '2026-04'. '지난달', '이번달' 같은 표현은 현재 날짜 기준으로 변환하세요.")
    public String getMonthlyAnalysis(String yearMonth) {
        try {
            AnalysisResponse response = analysisService.analyze(userId, storeId, yearMonth);
            return format(response);
        } catch (Exception e) {
            return yearMonth + " 데이터가 없습니다.";
        }
    }

    private String format(AnalysisResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append(response.yearMonth()).append(" 매출 분석 결과\n");
        sb.append("총 매출: ").append(formatAmount(response.totalRevenue())).append("\n\n");

        for (ChannelAnalysisResponse channel : response.channels()) {
            if (channel.revenue() == 0) continue;
            sb.append("[").append(channel.channel()).append("]\n");
            sb.append("매출: ").append(formatAmount(channel.revenue()))
              .append(" (비중: ").append(channel.revenueRatio()).append("%)\n");

            long totalCost = channel.costs().stream().mapToLong(CostItemResponse::amount).sum();
            sb.append("순이익: ").append(formatAmount(channel.revenue() - totalCost)).append("\n");

            for (CostItemResponse cost : channel.costs()) {
                if (cost.amount() == 0) continue;
                sb.append("  - ").append(cost.name()).append(": ")
                  .append(formatAmount(cost.amount())).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String formatAmount(long amount) {
        return String.format("%,d원", amount);
    }
}
