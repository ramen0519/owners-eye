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

            [광고비 특이사항]
            광고비가 [미집행] 상태라면 현황을 언급하고, 광고 집행 시 기대 효과를 데이터 기반으로 추론해서 제안하세요.

            [3개월 트렌드 분석 시 답변 형식]
            getThreeMonthTrendAnalysis 도구 결과에는 이미 계산된 [핵심 비용 3개월 비교] 표가 포함됩니다.
            반드시 아래 순서로 답변을 작성하세요.

            STEP 1 - 비교표 출력: 도구 결과에 포함된 [핵심 비용 3개월 비교] 표를 ```텍스트 코드블록```으로 그대로 출력합니다. 수정하거나 요약하지 마세요.

            STEP 2 - 인사이트 작성: 표 아래에 아래 구조로 인사이트를 작성합니다.

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
