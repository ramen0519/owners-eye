package com.ownerseye.ownerseye.domain.insight.domain.tools;

import com.ownerseye.ownerseye.domain.analysis.application.service.AnalysisService;
import com.ownerseye.ownerseye.global.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InsightTools {

    private final AnalysisService analysisService;

    @Tool(description = "특정 연월의 채널별 매출 및 순수익을 분석합니다. yearMonth는 yyyy-MM 형식입니다. (예: 2025-05)")
    public Object analyzeSales(
            @ToolParam(description = "분석할 연월 (yyyy-MM 형식, 예: 2025-05)") String yearMonth,
            ToolContext toolContext
    ) {
        try {
            Long userId = (Long) toolContext.getContext().get("userId");
            Long storeId = (Long) toolContext.getContext().get("storeId");
            return analysisService.analyze(userId, storeId, yearMonth);
        } catch (AppException e) {
            return "데이터 조회 실패: " + e.getMessage();
        }
    }
}
