package com.ownerseye.ownerseye.domain.insight.application.service;

import com.ownerseye.ownerseye.domain.analysis.application.dto.response.AnalysisResponse;
import com.ownerseye.ownerseye.domain.analysis.application.dto.response.ChannelAnalysisResponse;
import com.ownerseye.ownerseye.domain.analysis.application.dto.response.CostItemResponse;
import com.ownerseye.ownerseye.domain.analysis.application.service.AnalysisService;
import com.ownerseye.ownerseye.domain.insight.domain.prompt.InsightPrompts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class InsightService {

    private final ChatClient chatClient;
    private final AnalysisService analysisService;

    public InsightService(ChatClient.Builder chatClientBuilder, AnalysisService analysisService) {
        this.analysisService = analysisService;
        this.chatClient = chatClientBuilder
                .defaultSystem(InsightPrompts.SYSTEM)
                .defaultOptions(ChatOptions.builder()
                        .temperature(0.3)
                        .maxTokens(2000)
                        .build())
                .build();
    }

    public String generateInsight(Long userId, Long storeId, String yearMonth) {
        log.info("[InsightService] generateInsight - userId={}, storeId={}, yearMonth={}", userId, storeId, yearMonth);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate base = LocalDate.parse(yearMonth + "-01");
        String m1 = base.minusMonths(1).format(fmt);
        String m2 = base.minusMonths(2).format(fmt);

        AnalysisResponse current = analysisService.analyze(userId, storeId, yearMonth);
        AnalysisResponse prev1 = tryAnalyze(userId, storeId, m1);
        AnalysisResponse prev2 = tryAnalyze(userId, storeId, m2);

        String comparisonTable = buildComparisonTable(prev2, prev1, current);
        String channelDetail = formatChannelDetail(current);

        String userPrompt = InsightPrompts.analysisUser(yearMonth, comparisonTable, channelDetail);
        log.info("[InsightService] prompt 길이: {}", userPrompt.length());

        String insight = chatClient.prompt()
                .user(userPrompt)
                .call()
                .content();

        return comparisonTable + "\n\n" + insight;
    }

    private AnalysisResponse tryAnalyze(Long userId, Long storeId, String yearMonth) {
        try {
            AnalysisResponse r = analysisService.analyze(userId, storeId, yearMonth);
            return r.totalRevenue() > 0 ? r : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String buildComparisonTable(AnalysisResponse r1, AnalysisResponse r2, AnalysisResponse r3) {
        Map<String, long[]> c1 = extractCostAmounts(r1);
        Map<String, long[]> c2 = r2 != null ? extractCostAmounts(r2) : new LinkedHashMap<>();
        Map<String, long[]> c3 = extractCostAmounts(r3);

        Set<String> keys = new LinkedHashSet<>();
        keys.addAll(c1.keySet());
        keys.addAll(c2.keySet());
        keys.addAll(c3.keySet());

        String h1 = r1 != null ? r1.yearMonth() : "-";
        String h2 = r2 != null ? r2.yearMonth() : "-";
        String h3 = r3.yearMonth();

        long rev1 = r1 != null ? r1.totalRevenue() : 1;
        long rev2 = r2 != null ? r2.totalRevenue() : 1;
        long rev3 = r3.totalRevenue();

        StringBuilder sb = new StringBuilder();
        sb.append("### 핵심 비용 3개월 비교 (전체 매출 대비 비율)\n\n");
        sb.append("| 항목 | ").append(h1).append(" | ").append(h2).append(" | ").append(h3)
          .append(" | 전전→전 | 전→현 | 비고 |\n");
        sb.append("|---|---|---|---|---|---|---|\n");

        for (String key : keys) {
            long a1 = getAmt(c1, key);
            long a2 = getAmt(c2, key);
            long a3 = getAmt(c3, key);

            double ratio1 = toRatio(a1, rev1);
            double ratio2 = toRatio(a2, rev2);
            double ratio3 = toRatio(a3, rev3);
            double d1 = round1(ratio2 - ratio1);
            double d2 = round1(ratio3 - ratio2);

            sb.append("| ").append(key)
              .append(" | ").append(ratio1).append("% (").append(formatAmt(a1)).append(")")
              .append(" | ").append(ratio2).append("% (").append(formatAmt(a2)).append(")")
              .append(" | ").append(ratio3).append("% (").append(formatAmt(a3)).append(")")
              .append(" | ").append(String.format("%+.1f%%p", d1))
              .append(" | ").append(String.format("%+.1f%%p", d2))
              .append(" | ").append(buildTag(key, a3, d1, d2))
              .append(" |\n");
        }

        long p1 = r1 != null ? r1.totalRevenue() - sumAmt(c1) : 0;
        long p2 = r2 != null ? r2.totalRevenue() - sumAmt(c2) : 0;
        long p3 = rev3 - sumAmt(c3);
        double pr1 = toRatio(p1, rev1);
        double pr2 = toRatio(p2, rev2);
        double pr3 = toRatio(p3, rev3);
        double pd1 = round1(pr2 - pr1);
        double pd2 = round1(pr3 - pr2);
        String ptag = (pd1 < 0 && pd2 < 0) ? "3개월 연속 하락 ⚠" : (pd1 > 0 && pd2 > 0) ? "3개월 연속 상승" : "";

        sb.append("| **순이익** ")
          .append(" | **").append(pr1).append("% (").append(formatAmt(p1)).append(")**")
          .append(" | **").append(pr2).append("% (").append(formatAmt(p2)).append(")**")
          .append(" | **").append(pr3).append("% (").append(formatAmt(p3)).append(")**")
          .append(" | **").append(String.format("%+.1f%%p", pd1)).append("**")
          .append(" | **").append(String.format("%+.1f%%p", pd2)).append("**")
          .append(" | ").append(ptag)
          .append(" |\n");

        return sb.toString();
    }

    private String formatChannelDetail(AnalysisResponse r) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(r.yearMonth()).append(" 채널별 상세]\n");
        for (ChannelAnalysisResponse ch : r.channels()) {
            if (ch.revenue() == 0) continue;
            long totalCost = ch.costs().stream().mapToLong(CostItemResponse::amount).sum();
            long profit = ch.revenue() - totalCost;
            sb.append("▶ ").append(ch.channel())
              .append(": 매출 ").append(formatAmt(ch.revenue()))
              .append(" / 순이익 ").append(formatAmt(profit))
              .append(" (").append(toRatio(profit, ch.revenue())).append("%)\n");
            for (CostItemResponse cost : ch.costs()) {
                if (cost.amount() == 0 && !"광고비".equals(cost.name())) continue;
                String suffix = cost.amount() == 0 ? " [미집행]" : "";
                sb.append("  - ").append(cost.name()).append(": ")
                  .append(formatAmt(cost.amount()))
                  .append(" (").append(cost.ratio()).append("%)").append(suffix).append("\n");
            }
        }
        return sb.toString();
    }

    private Map<String, long[]> extractCostAmounts(AnalysisResponse r) {
        Map<String, long[]> totals = new LinkedHashMap<>();
        totals.put("광고비", new long[1]);
        if (r == null) return totals;
        for (ChannelAnalysisResponse ch : r.channels()) {
            if (ch.revenue() == 0) continue;
            for (CostItemResponse cost : ch.costs()) {
                totals.computeIfAbsent(cost.name(), k -> new long[1])[0] += cost.amount();
            }
        }
        return totals;
    }

    private String buildTag(String key, long currentAmount, double d1, double d2) {
        if ("광고비".equals(key) && currentAmount == 0) return "[미집행]";
        if (d1 > 0 && d2 > 0) return "[3개월 연속 상승 ⚠]";
        if (d1 < 0 && d2 < 0) return "[3개월 연속 하락]";
        if (d2 >= 2.0)  return String.format("[전월 대비 %.1f%%p 급등]", d2);
        if (d2 <= -2.0) return String.format("[전월 대비 %.1f%%p 급락]", Math.abs(d2));
        return "";
    }

    private long getAmt(Map<String, long[]> map, String key) {
        return map.getOrDefault(key, new long[1])[0];
    }

    private long sumAmt(Map<String, long[]> map) {
        return map.values().stream().mapToLong(v -> v[0]).sum();
    }

    private double toRatio(long amount, long total) {
        if (total == 0) return 0.0;
        return Math.round((double) amount / total * 1000) / 10.0;
    }

    private double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }

    private String formatAmt(long amount) {
        return String.format("%,d원", amount);
    }
}
