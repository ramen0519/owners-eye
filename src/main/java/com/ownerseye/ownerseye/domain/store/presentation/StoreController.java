package com.ownerseye.ownerseye.domain.store.presentation;

import com.ownerseye.ownerseye.domain.store.application.dto.request.StoreSaveRequest;
import com.ownerseye.ownerseye.domain.store.application.dto.response.StoreResponse;
import com.ownerseye.ownerseye.domain.store.application.service.StoreService;
import com.ownerseye.ownerseye.global.response.DataResponse;
import com.ownerseye.ownerseye.global.response.DefaultIdResponse;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "가게", description = "가게 CRUD API")
@RestController
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@RequestMapping("/api/stores")
@SecurityRequirement(name = "Bearer Authentication")
public class StoreController {

    private final StoreService storeService;

    @Operation(summary = "가게 등록")
    @PostMapping
    public ResponseEntity<DataResponse<DefaultIdResponse>> save(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody StoreSaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DataResponse.created(DefaultIdResponse.of(storeService.save(userId, request))));
    }

    @Operation(summary = "내 가게 목록 조회")
    @GetMapping
    public ResponseEntity<DataResponse<List<StoreResponse>>> findAll(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(DataResponse.from(storeService.findAllByUserId(userId)));
    }

    @Operation(summary = "가게 삭제")
    @DeleteMapping("/{storeId}")
    public ResponseEntity<DataResponse<Void>> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long storeId) {
        storeService.delete(userId, storeId);
        return ResponseEntity.ok(DataResponse.ok());
    }
}
