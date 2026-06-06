package com.ownerseye.ownerseye.domain.menu_insight.application.service;

import com.ownerseye.ownerseye.domain.analysis.application.service.AnalysisService;
import com.ownerseye.ownerseye.domain.analysis.exception.AnalysisException;
import com.ownerseye.ownerseye.domain.analysis.exception.code.AnalysisErrorCode;
import com.ownerseye.ownerseye.domain.menu_sale.persistence.entity.MenuSaleEntity;
import com.ownerseye.ownerseye.domain.menu_sale.persistence.mapper.MenuSaleMapper;
import com.ownerseye.ownerseye.domain.store.persistence.mapper.StoreMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class MenuInsightService {

    private final ChatClient chatClient;
    private final StoreMapper storeMapper;
    private final MenuSaleMapper menuSaleMapper;
    private final AnalysisService analysisService;

    private static final String SYSTEM_PROMPT = """
            당신은 소상공인 메뉴 판매 트렌드를 분석하는 AI 어시스턴트입니다.
            메뉴별 판매량과 채널별 매출 데이터를 함께 분석하여 실질적인 인사이트를 제공합니다.

            분석 시 다음을 포함하세요:
            1. 판매량 Top 3 / Bottom 3 메뉴
            2. 전월 대비 판매 증감이 큰 메뉴 (전월 데이터가 있을 경우)
            3. 판매가 저조하여 메뉴 정리를 검토할 항목
            4. 채널 매출 흐름과 메뉴 판매 트렌드 연계 의견

            가격이나 수익률 계산은 하지 마세요.
            답변은 한국어로 간결하게 작성하세요.
            """;

    public MenuInsightService(ChatClient.Builder chatClientBuilder,
                               StoreMapper storeMapper,
                               MenuSaleMapper menuSaleMapper,
                               AnalysisService analysisService) {
        this.storeMapper = storeMapper;
        this.menuSaleMapper = menuSaleMapper;
        this.analysisService = analysisService;
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultOptions(ChatOptions.builder()
                        .temperature(0.4)
                        .maxTokens(2000)
                        .build())
                .build();
    }

    public String generateInsight(Long userId, Long storeId, String yearMonthStr) {
        storeMapper.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new AnalysisException(AnalysisErrorCode.STORE_ACCESS_DENIED));

        LocalDate yearMonth = LocalDate.parse(yearMonthStr + "-01");
        LocalDate prevMonth = yearMonth.minusMonths(1);
        String prevMonthStr = prevMonth.getYear() + "-" + String.format("%02d", prevMonth.getMonthValue());

        List<MenuSaleEntity> currentSales = menuSaleMapper.findAllByStoreIdAndYearMonth(storeId, yearMonth);
        List<MenuSaleEntity> prevSales = menuSaleMapper.findAllByStoreIdAndYearMonth(storeId, prevMonth);

        String userPrompt = buildPrompt(yearMonthStr, prevMonthStr, currentSales, prevSales, userId, storeId);
        log.info("[MenuInsightService] 인사이트 생성 - storeId={}, yearMonth={}", storeId, yearMonthStr);

        return chatClient.prompt()
                .user(userPrompt)
                .call()
                .content();
    }

    private String buildPrompt(String yearMonth, String prevMonth,
                                List<MenuSaleEntity> current, List<MenuSaleEntity> prev,
                                Long userId, Long storeId) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(yearMonth).append(" 메뉴별 판매량 ===\n");

        if (current.isEmpty()) {
            sb.append("데이터 없음\n");
        } else {
            current.stream()
                    .sorted(Comparator.comparingInt(MenuSaleEntity::getQuantity).reversed())
                    .forEach(s -> sb.append("- ").append(s.getMenuName())
                            .append(": ").append(s.getQuantity()).append("개\n"));
        }

        sb.append("\n=== ").append(prevMonth).append(" 메뉴별 판매량 (전월) ===\n");
        if (prev.isEmpty()) {
            sb.append("데이터 없음\n");
        } else {
            prev.stream()
                    .sorted(Comparator.comparingInt(MenuSaleEntity::getQuantity).reversed())
                    .forEach(s -> sb.append("- ").append(s.getMenuName())
                            .append(": ").append(s.getQuantity()).append("개\n"));
        }

        try {
            var analysis = analysisService.analyze(userId, storeId, yearMonth);
            sb.append("\n=== ").append(yearMonth).append(" 채널별 매출 ===\n");
            sb.append("총 매출: ").append(String.format("%,d", analysis.totalRevenue())).append("원\n");
            analysis.channels().forEach(ch -> {
                if (ch.revenue() > 0) {
                    sb.append("- ").append(ch.channel()).append(": ")
                      .append(String.format("%,d", ch.revenue())).append("원")
                      .append(" (비중 ").append(ch.revenueRatio()).append("%)\n");
                }
            });
        } catch (Exception e) {
            log.warn("[MenuInsightService] 매출 데이터 조회 실패 - storeId={}, yearMonth={}", storeId, yearMonth);
        }

        sb.append("\n위 데이터를 분석하여 메뉴 전략 인사이트를 제공해주세요.");
        return sb.toString();
    }
}
