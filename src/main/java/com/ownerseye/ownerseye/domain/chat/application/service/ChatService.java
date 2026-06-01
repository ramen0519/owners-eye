package com.ownerseye.ownerseye.domain.chat.application.service;

import com.ownerseye.ownerseye.domain.analysis.application.service.AnalysisService;
import com.ownerseye.ownerseye.domain.chat.domain.tool.AnalysisTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
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

    // 대화당 최대 10개로 제한되나, 서버 재시작 전까지 누적됨. 운영 환경에서는 Redis 기반 ChatMemoryRepository로 교체 필요.
    private final MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
            .chatMemoryRepository(new InMemoryChatMemoryRepository())
            .maxMessages(10)
            .build();

    private static final String SYSTEM_PROMPT = """
            당신은 소상공인 매출 관리를 도와주는 AI 어시스턴트입니다.
            사장님의 실제 매출 데이터를 조회하거나 배민/쿠팡 공식 가이드를 참고하여 답변합니다.

            규칙:
            - 매출, 수익, 비용, 순이익 관련 질문은 반드시 도구(tool)를 사용해 실제 데이터를 조회하세요.
            - 플랫폼 광고 신청, 정산 방법, 수수료 정책은 제공된 문서를 참고하세요.
            - 이전 대화 내용을 기억하고 연속 질문에 자연스럽게 답하세요.
            - 모든 답변은 한국어로 친절하게 합니다.
            - 금액은 항상 원 단위로 표시하세요.
            """;

    public String chat(Long userId, Long storeId, String question) {
        AnalysisTool tool = new AnalysisTool(analysisService, userId, storeId);
        String conversationId = userId + ":" + storeId;

        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .build()
                .prompt()
                .user(question)
                .tools(tool)
                .advisors(
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .conversationId(conversationId)
                                .build(),
                        new QuestionAnswerAdvisor(vectorStore)
                )
                .call()
                .content();
    }
}
