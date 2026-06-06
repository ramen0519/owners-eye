package com.ownerseye.ownerseye.domain.chat.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatMemoryRepository implements ChatMemoryRepository {

    private static final String KEY_PREFIX = "chat:memory:";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<String> findConversationIds() {
        return List.of();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        try {
            String json = stringRedisTemplate.opsForValue().get(KEY_PREFIX + conversationId);
            if (json == null) return new ArrayList<>();

            List<Map<String, String>> list = objectMapper.readValue(json, new TypeReference<>() {});
            return list.stream()
                    .map(this::toMessage)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception e) {
            log.warn("[RedisChatMemory] 대화 기록 조회 실패 - conversationId={}", conversationId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        try {
            List<Map<String, String>> list = messages.stream()
                    .map(m -> Map.of(
                            "type", m.getMessageType().getValue(),
                            "content", m.getText() != null ? m.getText() : ""
                    ))
                    .toList();
            String json = objectMapper.writeValueAsString(list);
            stringRedisTemplate.opsForValue().set(KEY_PREFIX + conversationId, json, TTL);
        } catch (Exception e) {
            log.warn("[RedisChatMemory] 대화 기록 저장 실패 - conversationId={}", conversationId, e);
        }
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        stringRedisTemplate.delete(KEY_PREFIX + conversationId);
    }

    private Message toMessage(Map<String, String> map) {
        String type = map.get("type");
        String content = map.getOrDefault("content", "");
        return switch (type) {
            case "user" -> new UserMessage(content);
            case "assistant" -> new AssistantMessage(content);
            case "system" -> new SystemMessage(content);
            default -> null;
        };
    }
}
