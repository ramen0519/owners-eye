package com.ownerseye.ownerseye.domain.chat.presentation;

import com.ownerseye.ownerseye.domain.chat.application.dto.request.ChatRequest;
import com.ownerseye.ownerseye.domain.chat.application.dto.response.ChatResponse;
import com.ownerseye.ownerseye.domain.chat.application.service.ChatService;
import com.ownerseye.ownerseye.global.response.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Chat", description = "AI 매출 질의응답 채팅 API")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "AI 채팅", description = "자연어로 매출 관련 질문을 하면 AI가 답변합니다.")
    @PostMapping
    public DataResponse<ChatResponse> chat(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChatRequest request) {
        String answer = chatService.chat(userId, request.storeId(), request.question());
        return DataResponse.from(new ChatResponse(answer));
    }
}
