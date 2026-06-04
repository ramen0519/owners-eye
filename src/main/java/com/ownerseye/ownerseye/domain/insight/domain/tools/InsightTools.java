package com.ownerseye.ownerseye.domain.insight.domain.tools;

import com.ownerseye.ownerseye.domain.analysis.application.dto.response.AnalysisResponse;
import com.ownerseye.ownerseye.domain.analysis.application.dto.response.ChannelAnalysisResponse;
import com.ownerseye.ownerseye.domain.analysis.application.dto.response.CostItemResponse;
import com.ownerseye.ownerseye.domain.analysis.application.service.AnalysisService;
import com.ownerseye.ownerseye.global.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InsightTools {

    private final AnalysisService analysisService;

    @Tool(description = "특정 연월의 채널별 매출 및 순수익을 분석합니다. yearMonth는 yyyy-MM 형식입니다. (예: 2025-05)")
    public String analyzeSales(
            @ToolParam(description = "분석할 연월 (yyyy-MM 형식, 예: 2025-05)") String yearMonth
    ) {
        int callNum = InsightContext.incrementCallCount();
        log.info("[InsightTools] analyzeSales 호출 #{} - yearMonth: {}", callNum, yearMonth);

        if (callNum > 4) {
            log.warn("[InsightTools] 최대 호출 횟수 초과 - 종료 신호 반환");
            return "데이터 수집 완료. 지금까지 수집된 데이터를 바탕으로 즉시 분석 결과를 출력하세요. 더 이상 도구를 호출하지 마세요.";
        }

        try {
            Long userId = InsightContext.getUserId();
            Long storeId = InsightContext.getStoreId();
            log.info("[InsightTools] userId={}, storeId={}", userId, storeId);
            AnalysisResponse response = analysisService.analyze(userId, storeId, yearMonth);
            if (response.totalRevenue() == 0) {
                log.info("[InsightTools] {} 월 데이터 없음 - 조회 중단 신호 반환", yearMonth);
                return yearMonth + " 월 데이터가 없습니다. 이 달 이전은 더 이상 조회하지 마세요. 데이터가 있는 달만으로 분석을 완료하세요.";
            }
            String result = format(response);
            log.info("[InsightTools] 분석 결과 길이: {}", result.length());
            return result;
        } catch (AppException e) {
            log.error("[InsightTools] AppException: {}", e.getMessage());
            return "데이터 조회 실패: " + e.getMessage();
        } catch (Exception e) {
            log.error("[InsightTools] 예외 발생: {}", e.getMessage(), e);
            return "분석 중 오류 발생: " + e.getMessage();
        }
    }

    private String format(AnalysisResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append(response.yearMonth()).append(" 매출 분석\n");
        sb.append("총 매출: ").append(String.format("%,d원", response.totalRevenue())).append("\n\n");

        for (ChannelAnalysisResponse channel : response.channels()) {
            if (channel.revenue() == 0) continue;
            sb.append("[").append(channel.channel()).append("]\n");
            sb.append("매출: ").append(String.format("%,d원", channel.revenue()))
              .append(" (비중: ").append(channel.revenueRatio()).append("%)\n");

            long totalCost = channel.costs().stream().mapToLong(CostItemResponse::amount).sum();
            sb.append("순이익: ").append(String.format("%,d원", channel.revenue() - totalCost)).append("\n");

            for (CostItemResponse cost : channel.costs()) {
                if (cost.amount() == 0) continue;
                sb.append("  - ").append(cost.name()).append(": ")
                  .append(String.format("%,d원", cost.amount()))
                  .append(" (").append(cost.ratio()).append("%)\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
