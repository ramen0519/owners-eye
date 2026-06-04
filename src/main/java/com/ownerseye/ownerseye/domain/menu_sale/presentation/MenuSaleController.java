package com.ownerseye.ownerseye.domain.menu_sale.presentation;

import com.ownerseye.ownerseye.domain.menu_sale.application.service.MenuSaleService;
import com.ownerseye.ownerseye.global.response.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "MenuSale", description = "메뉴별 판매 데이터 업로드 API")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/menu-sales")
@RequiredArgsConstructor
public class MenuSaleController {

    private final MenuSaleService menuSaleService;

    @Operation(summary = "메뉴별 판매 엑셀 업로드", description = "AI가 엑셀을 파싱하여 메뉴별 판매 수량을 저장합니다. 같은 월 데이터는 교체됩니다.")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DataResponse<Void> upload(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long storeId,
            @RequestParam String yearMonth,
            @RequestPart MultipartFile file
    ) {
        menuSaleService.upload(userId, storeId, yearMonth, file);
        return DataResponse.ok();
    }
}
