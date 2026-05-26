package com.ownerseye.ownerseye.domain.chat.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatRequest(
        @NotBlank(message = "질문을 입력해주세요.") String question,
        @NotNull(message = "가게 ID를 입력해주세요.") Long storeId
) {
}
