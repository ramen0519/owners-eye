package com.ownerseye.ownerseye.domain.chat.application.service;

import com.ownerseye.ownerseye.domain.analysis.application.service.AnalysisService;
import com.ownerseye.ownerseye.domain.chat.domain.tool.AnalysisTool;
import com.ownerseye.ownerseye.domain.chat.domain.tool.MenuSaleTool;
import com.ownerseye.ownerseye.domain.chat.infrastructure.RedisChatMemoryRepository;
import com.ownerseye.ownerseye.domain.menu_sale.persistence.mapper.MenuSaleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatModel chatModel;
    private final VectorStore vectorStore;
    private final AnalysisService analysisService;
    private final MenuSaleMapper menuSaleMapper;
    private final RedisChatMemoryRepository redisChatMemoryRepository;

    private MessageWindowChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(redisChatMemoryRepository)
                .maxMessages(10)
                .build();
    }

    private static String buildSystemPrompt() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String thisMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String lastMonth = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return """
            당신은 소상공인 매출 관리를 도와주는 AI 어시스턴트입니다.
            사장님의 실제 매출 데이터를 조회하거나 배민/쿠팡 공식 가이드를 참고하여 답변합니다.

            [현재 날짜 정보]
            - 오늘: %s
            - 이번 달: %s
            - 지난 달: %s
            - "이번달", "이번 달", "이달"은 %s 로 처리하세요.
            - "지난달", "저번 달", "전달"은 %s 로 처리하세요.

            규칙:
            - 배민, 쿠팡, POS, 홀 등 채널별 매출/수익/비용/순이익 관련 질문은 반드시 도구(tool)를 사용해 실제 데이터를 조회하세요.
            - 도구로 조회한 결과에 쿠팡 데이터가 있으면 반드시 포함해서 답변하세요. 절대로 "제공할 수 없다"고 하지 마세요.
            - 메뉴별 판매량, 인기 메뉴, 판매 순위 관련 질문은 반드시 도구(tool)를 사용해 실제 데이터를 조회하세요.
            - 플랫폼 광고 신청, 정산 방법, 수수료 정책은 제공된 문서를 참고하세요.
            - 이전 대화 내용을 기억하고 연속 질문에 자연스럽게 답하세요.
            - 모든 답변은 한국어로 친절하게 합니다.
            - 금액은 항상 원 단위로 표시하세요.

            [비용 분석 원칙]
            수수료(중개이용료, 결제수수료)와 배달비는 플랫폼 정책상 조정 불가한 비용이므로 수치만 표시하고 "(조정 불가)"를 붙이세요.
            광고비, 재료비, 기타비용은 사장님이 직접 조절 가능한 항목입니다. 각 항목에 대해 아래 흐름으로 인사이트를 제공하세요.

            [인사이트 작성 흐름 - 각 비용 항목마다 적용]
            1. 수치 제시: 이번 달 금액과 매출 대비 비율(%), 전월 대비 증감(%p)을 명시합니다.
            2. 변화 원인 추론: 증가 또는 감소한 이유를 데이터 기반으로 추측합니다. (예: "배민1 매출이 늘면서 재료 소모가 증가했을 가능성", "광고비 증가 대비 매출도 함께 올랐으므로 광고 효율이 유지되고 있을 가능성")
            3. 효과 평가: 해당 변화가 순이익에 어떤 영향을 미쳤는지 평가합니다.
            4. 행동 추천: 다음 달에 사장님이 취할 수 있는 구체적인 행동을 제안합니다.

            [광고비 분석 필수 지침 - 항상 최우선으로 분석하세요]
            광고비는 사장님이 직접 컨트롤할 수 있는 핵심 비용입니다. 인사이트 작성 시 광고비를 반드시 가장 먼저, 가장 상세하게 다루세요.

            광고비가 [미집행] 상태(0원)인 경우:
            - "배민 우리가게클릭 및 쿠팡 광고가 3개월간 미집행 상태입니다"처럼 현황을 명시하세요.
            - 현재 총 매출 기준 광고비 1%, 2%, 3% 집행 시 예상 금액을 각각 계산해 제시하세요. (예: "현재 월 매출 3천만원 기준, 1% = 30만원, 3% = 90만원")
            - 배달앱 광고 집행 시 주문 노출 빈도 증가로 매출 5~15% 증가 효과가 일반적임을 언급하고, 현재 매출에 적용한 기대 수치를 제시하세요.
            - 권장 시작 예산과 어느 채널(배민1 vs 쿠팡)에 먼저 집행하면 좋을지 매출 비중을 근거로 추천하세요.

            광고비가 집행 중인 경우:
            - 전월 대비 광고비 %p 변화와 같은 기간 매출 변화율을 비교해 광고 ROI를 평가하세요.
            - 광고비 증가 → 매출 증가면 "광고 효율 유지", 광고비 증가 → 매출 감소면 "광고 효율 저하 - 타겟 재설정 검토" 방향으로 추천하세요.

            [3개월 트렌드 분석 시 답변 형식]
            [분석 데이터] 섹션에 3개월 비용 비교표와 채널 상세가 제공됩니다.
            비교표의 수치와 비고 태그를 반드시 참고하여 아래 구조로 인사이트를 작성하세요.
            수치를 다시 나열하지 말고, 원인 추론·효과 평가·행동 추천에 집중하세요.

            ## 📊 3개월 비용 트렌드 인사이트

            ### 1. 매출 흐름
            전월 대비 증감액과 증감률을 포함해 2~3줄로 서술합니다.

            ### 2. 항목별 분석
            비고 태그([3개월 연속 상승 ⚠], [미집행], [급등], [급락])가 붙은 항목을 우선으로, 없으면 변화가 큰 항목 순으로 분석합니다.
            각 항목마다 반드시 아래 3가지를 작성하세요:
            - **원인 추론**: 이 비율 변화가 생긴 이유를 데이터 기반으로 추측합니다.
            - **효과 평가**: 이 변화가 순이익률에 얼마나 영향을 미쳤는지, 금액으로 환산해 서술합니다.
            - **행동 추천**: 다음 달에 사장님이 취할 수 있는 구체적인 한 가지 행동을 제안합니다.

            ### 3. 채널 특이사항
            순이익이 마이너스이거나 구조적 문제가 있는 채널을 언급합니다.

            ### 4. 다음 달 실행 제안 3가지
            "항목 - 이유 - 구체적 행동" 형식으로 작성합니다.

            규칙:
            - 비교표에 없는 수치를 임의로 만들지 마세요.
            - "약", "대략", "비교적", "필요합니다", "요구됩니다" 같은 뭉뚱그린 표현 대신 구체적 수치와 추론을 씁니다.
            """.formatted(today, thisMonth, lastMonth, thisMonth, lastMonth);
    }

    public String chat(Long userId, Long storeId, String question) {
        AnalysisTool analysisTool = new AnalysisTool(analysisService, userId, storeId);
        MenuSaleTool menuSaleTool = new MenuSaleTool(menuSaleMapper, userId, storeId);
        String conversationId = userId + ":" + storeId;

        var memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory())
                .conversationId(conversationId)
                .build();

        // 트렌드/인사이트 질문: 데이터를 직접 로드해서 prompt에 포함, 비교표는 코드에서 prepend
        if (isTrendQuestion(question)) {
            String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String trendData = analysisTool.getThreeMonthTrendAnalysis(yearMonth);

            String enrichedPrompt = "[분석 데이터]\n" + trendData + "\n\n[사용자 질문] " + question;
            String insight = ChatClient.builder(chatModel)
                    .defaultSystem(buildSystemPrompt())
                    .build()
                    .prompt()
                    .user(enrichedPrompt)
                    .advisors(memoryAdvisor)
                    .call()
                    .content();

            String table = extractComparisonTable(trendData);
            return table.isEmpty() ? insight : "```\n" + table + "\n```\n\n" + insight;
        }

        // 일반 매출/메뉴 조회
        var promptSpec = ChatClient.builder(chatModel)
                .defaultSystem(buildSystemPrompt())
                .build()
                .prompt()
                .user(question)
                .tools(analysisTool, menuSaleTool);

        if (isPolicyQuestion(question)) {
            return promptSpec.advisors(memoryAdvisor, new QuestionAnswerAdvisor(vectorStore)).call().content();
        }
        return promptSpec.advisors(memoryAdvisor).call().content();
    }

    private boolean isTrendQuestion(String q) {
        // 특정 연월이 명시된 경우는 tool로 처리
        if (q.matches(".*\\d{4}[-년].*") || q.matches(".*\\d{1,2}월.*")) return false;
        for (String t : new String[]{"인사이트", "트렌드", "3개월"}) {
            if (q.contains(t)) return true;
        }
        return false;
    }

    private boolean isPolicyQuestion(String question) {
        for (String t : new String[]{"신청", "방법", "어떻게 하면", "수수료 정책", "정산 방법", "약관", "규정", "가이드"}) {
            if (question.contains(t)) return true;
        }
        return false;
    }

    private String extractComparisonTable(String trendData) {
        int start = trendData.indexOf("[핵심 비용");
        if (start < 0) return "";
        int end = trendData.indexOf("\n\n[", start);
        if (end < 0) end = trendData.length();
        return trendData.substring(start, end).trim();
    }
}
