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
