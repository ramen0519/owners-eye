package com.ownerseye.ownerseye.domain.example.presentation;

import com.ownerseye.ownerseye.domain.example.application.dto.request.ExampleRequest;
import com.ownerseye.ownerseye.domain.example.application.dto.response.ExampleResponse;
import com.ownerseye.ownerseye.domain.example.domain.service.ExampleService;
import com.ownerseye.ownerseye.global.response.DataResponse;
import com.ownerseye.ownerseye.global.response.DefaultIdResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@RequestMapping("/api/examples")
public class ExampleController {

    private final ExampleService exampleService;

    @Operation(summary = "예시 이름 저장 API", description = "이름을 받아서 DB에 저장합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "실패")
    })
    @PostMapping
    public ResponseEntity<DataResponse<DefaultIdResponse>> save(@RequestBody ExampleRequest request) {
        return ResponseEntity.ok(
                DataResponse.created(DefaultIdResponse.of(exampleService.save(request.name())))
        );
    }

    @Operation(summary = "예시 이름 조회 API", description = "id에 해당하는 이름을 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DataResponse<ExampleResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(DataResponse.from(exampleService.findById(id)));
    }
}
