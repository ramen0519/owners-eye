package com.ownerseye.ownerseye.domain.upload.presentation;

import com.ownerseye.ownerseye.domain.upload.application.service.BaeminParserService;
import com.ownerseye.ownerseye.domain.upload.application.service.PosParserService;
import com.ownerseye.ownerseye.global.response.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@SecurityRequirement(name = "Bearer Authentication")
public class UploadController {

    private final PosParserService posParserService;
    private final BaeminParserService baeminParserService;

    @Operation(summary = "POS 엑셀 업로드")
    @PostMapping(value = "/pos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DataResponse<Void>> uploadPos(
            @AuthenticationPrincipal Long userId,
            @RequestParam String yearMonth,
            @RequestPart MultipartFile file
    ) {
        LocalDate date = LocalDate.parse(yearMonth + "-01");
        posParserService.parse(userId, file, date);
        return ResponseEntity.ok(DataResponse.ok());
    }

    @Operation(summary = "배민 엑셀 업로드")
    @PostMapping(value = "/baemin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DataResponse<Void>> uploadBaemin(
            @AuthenticationPrincipal Long userId,
            @RequestParam String yearMonth,
            @RequestPart MultipartFile file
    ) {
        LocalDate date = LocalDate.parse(yearMonth + "-01");
        baeminParserService.parse(userId, file, date);
        return ResponseEntity.ok(DataResponse.ok());
    }
}
