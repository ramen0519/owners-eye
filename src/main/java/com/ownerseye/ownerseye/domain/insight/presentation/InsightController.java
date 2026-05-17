package com.ownerseye.ownerseye.domain.insight.presentation;

import com.ownerseye.ownerseye.domain.insight.application.service.InsightService;
import com.ownerseye.ownerseye.global.response.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI 인사이트", description = "매출 분석 AI 인사이트 API")
@RestController
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@RequestMapping("/api/insight")
@SecurityRequirement(name = "Bearer Authentication")
public class InsightController {

    private final InsightService insightService;

    @Operation(summary = "AI 매출 인사이트 생성")
    @GetMapping
    public ResponseEntity<DataResponse<String>> generateInsight(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long storeId,
            @RequestParam String yearMonth) {
        return ResponseEntity.ok(DataResponse.from(insightService.generateInsight(userId, storeId, yearMonth)));
    }
}
