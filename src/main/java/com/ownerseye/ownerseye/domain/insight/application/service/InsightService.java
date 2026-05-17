package com.ownerseye.ownerseye.domain.insight.application.service;

import com.ownerseye.ownerseye.domain.insight.domain.prompt.InsightPrompts;
import com.ownerseye.ownerseye.domain.insight.domain.tools.InsightTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class InsightService {

    private final ChatClient chatClient;

    public InsightService(ChatClient.Builder chatClientBuilder, InsightTools insightTools) {
        this.chatClient = chatClientBuilder
                .defaultSystem(InsightPrompts.SYSTEM)
                .defaultTools(insightTools)
                .defaultOptions(ChatOptions.builder()
                        .temperature(0.3)
                        .maxTokens(2000)
                        .build())
                .build();
    }

    public String generateInsight(Long userId, Long storeId, String yearMonth) {
        return chatClient.prompt()
                .user(InsightPrompts.analysisUser(yearMonth))
                .toolContext(Map.of("userId", userId, "storeId", storeId))
                .call()
                .content();
    }
}
