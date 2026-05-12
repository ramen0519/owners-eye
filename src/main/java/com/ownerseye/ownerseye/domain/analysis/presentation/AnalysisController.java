package com.ownerseye.ownerseye.domain.analysis.presentation;

import com.ownerseye.ownerseye.domain.analysis.application.dto.response.AnalysisResponse;
import com.ownerseye.ownerseye.domain.analysis.application.service.AnalysisService;
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

@Tag(name = "수익 분석", description = "채널별 수익 분석 API")
@RestController
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@RequestMapping("/api/analysis")
@SecurityRequirement(name = "Bearer Authentication")
public class AnalysisController {

    private final AnalysisService analysisService;

    @Operation(summary = "채널별 수익 분석 조회")
    @GetMapping
    public ResponseEntity<DataResponse<AnalysisResponse>> analyze(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long storeId,
            @RequestParam String yearMonth) {
        return ResponseEntity.ok(DataResponse.from(analysisService.analyze(userId, storeId, yearMonth)));
    }
}
