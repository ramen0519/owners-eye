package com.ownerseye.ownerseye.domain.chat.domain.tool;

import com.ownerseye.ownerseye.domain.analysis.application.dto.response.AnalysisResponse;
import com.ownerseye.ownerseye.domain.analysis.application.dto.response.ChannelAnalysisResponse;
import com.ownerseye.ownerseye.domain.analysis.application.dto.response.CostItemResponse;
import com.ownerseye.ownerseye.domain.analysis.application.service.AnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.LinkedHashMap;

@Slf4j
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
            return formatDetail(response);
        } catch (Exception e) {
            log.error("[AnalysisTool] 분석 조회 실패: userId={}, storeId={}, yearMonth={}", userId, storeId, yearMonth, e);
            return yearMonth + " 데이터가 없습니다.";
        }
    }

    @Tool(description = "최근 3개월 매출 트렌드를 분석합니다. yearMonth는 기준 월('yyyy-MM' 형식)입니다. 인사이트 요청, 트렌드 분석, 비용 변화 분석 시 이 도구를 우선 사용하세요.")
    public String getThreeMonthTrendAnalysis(String yearMonth) {
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
            LocalDate base = LocalDate.parse(yearMonth + "-01");
            String m1 = base.minusMonths(1).format(fmt);
            String m2 = base.minusMonths(2).format(fmt);

            AnalysisResponse current = analysisService.analyze(userId, storeId, yearMonth);
            AnalysisResponse prev1 = tryAnalyze(m1);
            AnalysisResponse prev2 = tryAnalyze(m2);

            StringBuilder sb = new StringBuilder();
            sb.append("=== 최근 3개월 매출 트렌드 ===\n\n");

            if (prev2 != null) sb.append(formatSummary(prev2));
            if (prev1 != null) sb.append(formatSummary(prev1));
            sb.append(formatDetail(current));

            return sb.toString();
        } catch (Exception e) {
            log.error("[AnalysisTool] 3개월 트렌드 조회 실패: yearMonth={}", yearMonth, e);
            return "트렌드 분석 중 오류가 발생했습니다.";
        }
    }

    private AnalysisResponse tryAnalyze(String yearMonth) {
        try {
            return analysisService.analyze(userId, storeId, yearMonth);
        } catch (Exception e) {
            return null;
        }
    }

    // 이전 월: 핵심 비율만 요약
    private String formatSummary(AnalysisResponse r) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(r.yearMonth()).append(" 요약]\n");
        sb.append("총 매출: ").append(formatAmount(r.totalRevenue())).append("\n");

        // 전체 매출 기준 핵심 비용 비율 집계
        Map<String, long[]> costTotals = new LinkedHashMap<>();
        long totalCostAll = 0;
        long totalProfit = 0;

        for (ChannelAnalysisResponse ch : r.channels()) {
            if (ch.revenue() == 0) continue;
            for (CostItemResponse cost : ch.costs()) {
                costTotals.computeIfAbsent(cost.name(), k -> new long[1])[0] += cost.amount();
                totalCostAll += cost.amount();
            }
        }
        totalProfit = r.totalRevenue() - totalCostAll;

        for (Map.Entry<String, long[]> entry : costTotals.entrySet()) {
            long amt = entry.getValue()[0];
            if (amt == 0) continue;
            double ratio = r.totalRevenue() == 0 ? 0 : Math.round((double) amt / r.totalRevenue() * 1000) / 10.0;
            sb.append("  ").append(entry.getKey()).append(": ").append(formatAmount(amt))
              .append(" (매출 대비 ").append(ratio).append("%)\n");
        }

        double profitRatio = r.totalRevenue() == 0 ? 0 : Math.round((double) totalProfit / r.totalRevenue() * 1000) / 10.0;
        sb.append("  순이익: ").append(formatAmount(totalProfit))
          .append(" (매출 대비 ").append(profitRatio).append("%)\n\n");
        return sb.toString();
    }

    // 현재 월: 채널별 전체 상세
    private String formatDetail(AnalysisResponse r) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(r.yearMonth()).append(" 상세 분석]\n");
        sb.append("총 매출: ").append(formatAmount(r.totalRevenue())).append("\n\n");

        for (ChannelAnalysisResponse ch : r.channels()) {
            if (ch.revenue() == 0) continue;
            sb.append("▶ ").append(ch.channel()).append("\n");
            sb.append("  매출: ").append(formatAmount(ch.revenue()))
              .append(" (전체 비중 ").append(ch.revenueRatio()).append("%)\n");

            long totalCost = ch.costs().stream().mapToLong(CostItemResponse::amount).sum();
            long profit = ch.revenue() - totalCost;
            double profitRatio = ch.revenue() == 0 ? 0 : Math.round((double) profit / ch.revenue() * 1000) / 10.0;

            for (CostItemResponse cost : ch.costs()) {
                if (cost.amount() == 0) continue;
                sb.append("  - ").append(cost.name()).append(": ")
                  .append(formatAmount(cost.amount()))
                  .append(" (매출 대비 ").append(cost.ratio()).append("%)\n");
            }
            sb.append("  순이익: ").append(formatAmount(profit))
              .append(" (매출 대비 ").append(profitRatio).append("%)\n\n");
        }
        return sb.toString();
    }

    private String formatAmount(long amount) {
        return String.format("%,d원", amount);
    }
}
