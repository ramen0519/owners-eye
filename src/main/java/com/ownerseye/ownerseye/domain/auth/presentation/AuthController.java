package com.ownerseye.ownerseye.domain.auth.presentation;

import com.ownerseye.ownerseye.domain.auth.application.dto.request.LoginRequest;
import com.ownerseye.ownerseye.domain.auth.application.dto.request.SignupRequest;
import com.ownerseye.ownerseye.domain.auth.application.dto.response.TokenResponse;
import com.ownerseye.ownerseye.domain.auth.domain.service.AuthService;
import com.ownerseye.ownerseye.global.response.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<DataResponse<Void>> signup(@RequestBody @Valid SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok(DataResponse.ok());
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<DataResponse<TokenResponse>> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(DataResponse.from(authService.login(request)));
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/reissue")
    public ResponseEntity<DataResponse<TokenResponse>> reissue(@RequestHeader("Refresh-Token") String refreshToken) {
        return ResponseEntity.ok(DataResponse.from(authService.reissue(refreshToken)));
    }

    @Operation(summary = "로그아웃")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/logout")
    public ResponseEntity<DataResponse<Void>> logout(@AuthenticationPrincipal Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok(DataResponse.ok());
    }
}
