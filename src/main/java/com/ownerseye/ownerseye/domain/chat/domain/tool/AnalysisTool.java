package com.ownerseye.ownerseye.domain.chat.domain.tool;

import com.ownerseye.ownerseye.domain.analysis.application.dto.response.AnalysisResponse;
import com.ownerseye.ownerseye.domain.analysis.application.dto.response.ChannelAnalysisResponse;
import com.ownerseye.ownerseye.domain.analysis.application.dto.response.CostItemResponse;
import com.ownerseye.ownerseye.domain.analysis.application.service.AnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    @Tool(description = "특정 연월의 가게 매출을 분석합니다. yearMonth는 'yyyy-MM' 형식입니다.")
    public String getMonthlyAnalysis(String yearMonth) {
        try {
            return formatDetail(analysisService.analyze(userId, storeId, yearMonth));
        } catch (Exception e) {
            log.error("[AnalysisTool] 분석 조회 실패: yearMonth={}", yearMonth, e);
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
            sb.append("=== 최근 3개월 매출 트렌드 데이터 ===\n\n");

            sb.append("[월별 총 매출]\n");
            if (prev2 != null) sb.append(prev2.yearMonth()).append(": ").append(formatAmount(prev2.totalRevenue())).append("\n");
            if (prev1 != null) sb.append(prev1.yearMonth()).append(": ").append(formatAmount(prev1.totalRevenue())).append("\n");
            sb.append(current.yearMonth()).append(": ").append(formatAmount(current.totalRevenue())).append("\n\n");

            if (prev2 != null && prev1 != null) {
                sb.append(buildComparisonTable(prev2, prev1, current));
            } else if (prev1 != null) {
                sb.append(buildComparisonTable(prev1, current));
            }

            sb.append("\n").append(formatDetail(current));
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

    private String buildComparisonTable(AnalysisResponse r1, AnalysisResponse r2, AnalysisResponse r3) {
        Map<String, long[]> c1 = extractCostAmounts(r1);
        Map<String, long[]> c2 = extractCostAmounts(r2);
        Map<String, long[]> c3 = extractCostAmounts(r3);

        Set<String> keys = new LinkedHashSet<>();
        keys.addAll(c1.keySet());
        keys.addAll(c2.keySet());
        keys.addAll(c3.keySet());

        StringBuilder sb = new StringBuilder();
        sb.append("[핵심 비용 3개월 비교 - 전체 매출 대비 비율]\n");
        sb.append(String.format("%-10s | %-20s | %-20s | %-20s | %8s | %8s | 비고\n",
                "항목", r1.yearMonth(), r2.yearMonth(), r3.yearMonth(), "전전→전", "전→현"));
        sb.append("-".repeat(110)).append("\n");

        for (String key : keys) {
            long a1 = getAmount(c1, key);
            long a2 = getAmount(c2, key);
            long a3 = getAmount(c3, key);
            double ratio1 = toRatio(a1, r1.totalRevenue());
            double ratio2 = toRatio(a2, r2.totalRevenue());
            double ratio3 = toRatio(a3, r3.totalRevenue());
            double d1 = round1(ratio2 - ratio1);
            double d2 = round1(ratio3 - ratio2);

            sb.append(String.format("%-10s | %5.1f%% (%-10s) | %5.1f%% (%-10s) | %5.1f%% (%-10s) | %+7.1f%%p | %+7.1f%%p | %s\n",
                    key,
                    ratio1, formatAmount(a1),
                    ratio2, formatAmount(a2),
                    ratio3, formatAmount(a3),
                    d1, d2, buildTag(key, a3, d1, d2)));
        }

        long p1 = r1.totalRevenue() - sumAmounts(c1);
        long p2 = r2.totalRevenue() - sumAmounts(c2);
        long p3 = r3.totalRevenue() - sumAmounts(c3);
        double pr1 = toRatio(p1, r1.totalRevenue());
        double pr2 = toRatio(p2, r2.totalRevenue());
        double pr3 = toRatio(p3, r3.totalRevenue());
        double pd1 = round1(pr2 - pr1);
        double pd2 = round1(pr3 - pr2);
        String ptag = (pd1 < 0 && pd2 < 0) ? "[3개월 연속 하락 ⚠]" : (pd1 > 0 && pd2 > 0) ? "[3개월 연속 상승]" : "";

        sb.append(String.format("%-10s | %5.1f%% (%-10s) | %5.1f%% (%-10s) | %5.1f%% (%-10s) | %+7.1f%%p | %+7.1f%%p | %s\n",
                "▶ 순이익",
                pr1, formatAmount(p1),
                pr2, formatAmount(p2),
                pr3, formatAmount(p3),
                pd1, pd2, ptag));

        return sb.toString();
    }

    private String buildComparisonTable(AnalysisResponse r1, AnalysisResponse r2) {
        Map<String, long[]> c1 = extractCostAmounts(r1);
        Map<String, long[]> c2 = extractCostAmounts(r2);

        Set<String> keys = new LinkedHashSet<>();
        keys.addAll(c1.keySet());
        keys.addAll(c2.keySet());

        StringBuilder sb = new StringBuilder();
        sb.append("[핵심 비용 2개월 비교 - 전체 매출 대비 비율]\n");

        for (String key : keys) {
            long a1 = getAmount(c1, key);
            long a2 = getAmount(c2, key);
            double ratio1 = toRatio(a1, r1.totalRevenue());
            double ratio2 = toRatio(a2, r2.totalRevenue());
            double d = round1(ratio2 - ratio1);
            sb.append(String.format("%-10s: %s %.1f%% → %s %.1f%% (%+.1f%%p) %s\n",
                    key, r1.yearMonth(), ratio1, r2.yearMonth(), ratio2, d, buildTag(key, a2, 0, d)));
        }

        long p1 = r1.totalRevenue() - sumAmounts(c1);
        long p2 = r2.totalRevenue() - sumAmounts(c2);
        double pd = round1(toRatio(p2, r2.totalRevenue()) - toRatio(p1, r1.totalRevenue()));
        sb.append(String.format("%-10s: %s %.1f%% → %s %.1f%% (%+.1f%%p)\n",
                "▶ 순이익", r1.yearMonth(), toRatio(p1, r1.totalRevenue()), r2.yearMonth(), toRatio(p2, r2.totalRevenue()), pd));

        return sb.toString();
    }

    private Map<String, long[]> extractCostAmounts(AnalysisResponse r) {
        Map<String, long[]> totals = new LinkedHashMap<>();
        totals.put("광고비", new long[1]);
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

    private String formatDetail(AnalysisResponse r) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(r.yearMonth()).append(" 채널별 상세]\n");

        for (ChannelAnalysisResponse ch : r.channels()) {
            if (ch.revenue() == 0) continue;
            long totalCost = ch.costs().stream().mapToLong(CostItemResponse::amount).sum();
            long profit = ch.revenue() - totalCost;

            sb.append("▶ ").append(ch.channel())
              .append(": 매출 ").append(formatAmount(ch.revenue()))
              .append(" (비중 ").append(ch.revenueRatio()).append("%)")
              .append(" / 순이익 ").append(formatAmount(profit))
              .append(" (").append(toRatio(profit, ch.revenue())).append("%)\n");

            for (CostItemResponse cost : ch.costs()) {
                if (cost.amount() == 0 && !"광고비".equals(cost.name())) continue;
                String suffix = (cost.amount() == 0 && "광고비".equals(cost.name())) ? " [미집행]" : "";
                sb.append("  - ").append(cost.name()).append(": ")
                  .append(formatAmount(cost.amount()))
                  .append(" (").append(cost.ratio()).append("%)").append(suffix).append("\n");
            }
        }
        return sb.toString();
    }

    private long getAmount(Map<String, long[]> map, String key) {
        return map.getOrDefault(key, new long[1])[0];
    }

    private long sumAmounts(Map<String, long[]> map) {
        return map.values().stream().mapToLong(v -> v[0]).sum();
    }

    private double toRatio(long amount, long total) {
        if (total == 0) return 0.0;
        return Math.round((double) amount / total * 1000) / 10.0;
    }

    private double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }

    private String formatAmount(long amount) {
        return String.format("%,d원", amount);
    }
}
