package com.ownerseye.ownerseye.domain.fixed_cost.presentation;

import com.ownerseye.ownerseye.domain.fixed_cost.application.dto.request.FixedCostSaveRequest;
import com.ownerseye.ownerseye.domain.fixed_cost.application.dto.request.FixedCostUpdateRequest;
import com.ownerseye.ownerseye.domain.fixed_cost.application.dto.response.FixedCostResponse;
import com.ownerseye.ownerseye.domain.fixed_cost.application.service.FixedCostService;
import com.ownerseye.ownerseye.global.response.DataResponse;
import com.ownerseye.ownerseye.global.response.DefaultIdResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "고정비용", description = "고정비용 CRUD API")
@RestController
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@RequestMapping("/api/fixed-costs")
@SecurityRequirement(name = "Bearer Authentication")
public class FixedCostController {

    private final FixedCostService fixedCostService;

    @Operation(summary = "고정비용 저장", description = "가게의 특정 월 고정비용을 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 연월 형식 또는 필수 값 누락"),
            @ApiResponse(responseCode = "403", description = "해당 가게에 대한 권한 없음"),
            @ApiResponse(responseCode = "404", description = "가게 정보 없음"),
            @ApiResponse(responseCode = "409", description = "해당 월 고정비용이 이미 존재함")
    })
    @PostMapping
    public ResponseEntity<DataResponse<DefaultIdResponse>> save(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody FixedCostSaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DataResponse.created(DefaultIdResponse.of(fixedCostService.save(userId, request))));
    }

    @Operation(summary = "고정비용 단건 조회", description = "가게 ID와 연월로 고정비용을 조회합니다. (yearMonth 형식: yyyy-MM)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 연월 형식"),
            @ApiResponse(responseCode = "403", description = "해당 가게에 대한 권한 없음"),
            @ApiResponse(responseCode = "404", description = "가게 정보 없음 또는 고정비용 정보 없음")
    })
    @GetMapping
    public ResponseEntity<DataResponse<FixedCostResponse>> findByStoreIdAndYearMonth(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long storeId,
            @RequestParam String yearMonth) {
        return ResponseEntity.ok(
                DataResponse.from(fixedCostService.findByStoreIdAndYearMonth(userId, storeId, yearMonth))
        );
    }

    @Operation(summary = "가게 고정비용 목록 조회", description = "특정 가게의 모든 월 고정비용 목록을 최신 순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "해당 가게에 대한 권한 없음"),
            @ApiResponse(responseCode = "404", description = "가게 정보 없음")
    })
    @GetMapping("/stores/{storeId}")
    public ResponseEntity<DataResponse<List<FixedCostResponse>>> findAllByStoreId(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long storeId) {
        return ResponseEntity.ok(
                DataResponse.from(fixedCostService.findAllByStoreId(userId, storeId))
        );
    }

    @Operation(summary = "고정비용 수정", description = "고정비용 ID로 고정비용 항목을 수정합니다. null 필드는 기존 값을 유지합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "해당 가게에 대한 권한 없음"),
            @ApiResponse(responseCode = "404", description = "고정비용 정보 없음")
    })
    @PutMapping("/{fixedCostId}")
    public ResponseEntity<DataResponse<Void>> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long fixedCostId,
            @RequestBody FixedCostUpdateRequest request) {
        fixedCostService.update(userId, fixedCostId, request);
        return ResponseEntity.ok(DataResponse.ok());
    }

    @Operation(summary = "고정비용 삭제", description = "고정비용 ID로 고정비용 항목을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "해당 가게에 대한 권한 없음"),
            @ApiResponse(responseCode = "404", description = "고정비용 정보 없음")
    })
    @DeleteMapping("/{fixedCostId}")
    public ResponseEntity<DataResponse<Void>> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long fixedCostId) {
        fixedCostService.delete(userId, fixedCostId);
        return ResponseEntity.ok(DataResponse.ok());
    }
}
