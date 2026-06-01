package com.ownerseye.ownerseye.domain.insight.application.service;

import com.ownerseye.ownerseye.domain.insight.domain.prompt.InsightPrompts;
import com.ownerseye.ownerseye.domain.insight.domain.tools.InsightContext;
import com.ownerseye.ownerseye.domain.insight.domain.tools.InsightTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InsightService {

    private final ChatClient chatClient;
    private final InsightTools insightTools;

    public InsightService(ChatClient.Builder chatClientBuilder, InsightTools insightTools) {
        this.insightTools = insightTools;
        this.chatClient = chatClientBuilder
                .defaultSystem(InsightPrompts.SYSTEM)
                .defaultOptions(ChatOptions.builder()
                        .temperature(0.3)
                        .maxTokens(2000)
                        .build())
                .build();
    }

    public String generateInsight(Long userId, Long storeId, String yearMonth) {
        log.info("[InsightService] generateInsight 호출 - userId={}, storeId={}, yearMonth={}", userId, storeId, yearMonth);
        ToolCallback[] toolCallbacks = ToolCallbacks.from(insightTools);
        log.info("[InsightService] 등록된 도구 수: {}", toolCallbacks.length);
        try {
            InsightContext.set(userId, storeId);
            String result = chatClient.prompt()
                    .user(InsightPrompts.analysisUser(yearMonth))
                    .toolCallbacks(toolCallbacks)
                    .options(OpenAiChatOptions.builder().toolChoice("auto").build())
                    .call()
                    .content();
            log.info("[InsightService] AI 응답 완료 - 길이: {}", result != null ? result.length() : 0);
            return result;
        } finally {
            InsightContext.clear();
        }
    }
}
