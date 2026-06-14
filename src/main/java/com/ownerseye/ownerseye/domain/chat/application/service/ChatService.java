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
            수수료(중개이용료, 결제수수료, 배달비)는 플랫폼 정책상 사장님이 조정할 수 없는 고정 비용입니다.
            인사이트는 반드시 사장님이 실제로 조절 가능한 아래 3가지 항목에 집중하세요.

            [핵심 분석 항목]
            1. 광고비: 매출 대비 비율 계산. 3% 이하 효율적 / 5% 초과 과다 지출로 조정 권장. 0원이면 광고 미집행 언급.
            2. 재료비: 원가율 계산. 30% 이하 양호 / 35% 초과 메뉴 가격 조정 또는 원가 절감 검토 권장.
            3. 기타비용: 비율이 높으면 어떤 지출인지 점검 권장.

            [3개월 트렌드 분석 시 답변 방식]
            getThreeMonthTrendAnalysis 도구를 사용한 경우 아래 구조로 상세하게 답변하세요:

            1. 전체 매출 추이: 3개월 총 매출 흐름을 수치와 함께 서술.
            2. 핵심 비용 비율 변화 (월별 비교 테이블 형태로):
               - 광고비%, 재료비%, 기타%, 순이익률%을 3개월 나란히 비교.
               - 각 항목이 이전 달 대비 몇 %p 증가/감소했는지 명시.
            3. 변화의 원인 및 효과 분석:
               - 비율이 오른 항목은 "왜 올랐는지" 가능한 원인을 언급.
               - 비율이 내린 항목은 "어떤 효과가 있었는지" 긍정적으로 평가.
            4. 채널별 특이사항: 순이익률이 낮거나 변동이 큰 채널을 짚어주세요.
            5. 실행 제안: 다음 달에 사장님이 바로 취할 수 있는 구체적인 행동 1~3가지.

            답변은 충분히 길고 구체적으로 작성하세요. 수치 없는 뭉뚱그린 표현은 금지입니다.
            """.formatted(today, thisMonth, lastMonth, thisMonth, lastMonth);
    }

    public String chat(Long userId, Long storeId, String question) {
        AnalysisTool analysisTool = new AnalysisTool(analysisService, userId, storeId);
        MenuSaleTool menuSaleTool = new MenuSaleTool(menuSaleMapper, userId, storeId);
        String conversationId = userId + ":" + storeId;

        return ChatClient.builder(chatModel)
                .defaultSystem(buildSystemPrompt())
                .build()
                .prompt()
                .user(question)
                .tools(analysisTool, menuSaleTool)
                .advisors(
                        MessageChatMemoryAdvisor.builder(chatMemory())
                                .conversationId(conversationId)
                                .build(),
                        new QuestionAnswerAdvisor(vectorStore)
                )
                .call()
                .content();
    }
}
