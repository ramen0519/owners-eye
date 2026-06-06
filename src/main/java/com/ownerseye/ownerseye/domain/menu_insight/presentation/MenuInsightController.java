package com.ownerseye.ownerseye.domain.menu_insight.presentation;

import com.ownerseye.ownerseye.domain.menu_insight.application.service.MenuInsightService;
import com.ownerseye.ownerseye.global.response.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "MenuInsight", description = "메뉴 판매 인사이트 API")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/menu-insight")
@RequiredArgsConstructor
public class MenuInsightController {

    private final MenuInsightService menuInsightService;

    @Operation(summary = "메뉴 인사이트 생성",
            description = "메뉴별 판매량과 채널별 매출을 AI가 분석하여 인사이트와 가격 최적화 추천을 제공합니다.")
    @GetMapping
    public DataResponse<String> getInsight(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long storeId,
            @RequestParam String yearMonth
    ) {
        return DataResponse.from(menuInsightService.generateInsight(userId, storeId, yearMonth));
    }
}
