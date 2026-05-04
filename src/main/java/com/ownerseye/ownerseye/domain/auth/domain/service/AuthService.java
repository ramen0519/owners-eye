package com.ownerseye.ownerseye.domain.auth.domain.service;

import com.ownerseye.ownerseye.domain.auth.application.dto.request.LoginRequest;
import com.ownerseye.ownerseye.domain.auth.application.dto.request.SignupRequest;
import com.ownerseye.ownerseye.domain.auth.application.dto.response.TokenResponse;
import com.ownerseye.ownerseye.domain.auth.exception.AuthException;
import com.ownerseye.ownerseye.domain.auth.exception.code.AuthErrorCode;
import com.ownerseye.ownerseye.domain.auth.persistence.entity.UserEntity;
import com.ownerseye.ownerseye.domain.auth.persistence.mapper.UserMapper;
import com.ownerseye.ownerseye.global.jwt.JwtProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthService {

    private static final String REFRESH_KEY = "refresh:";
    private static final Duration REFRESH_TTL = Duration.ofDays(14);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public void signup(SignupRequest request) {
        if (userMapper.existsByEmail(request.email())) {
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        UserEntity user = UserEntity.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .build();

        userMapper.save(user);
    }

    public TokenResponse login(LoginRequest request) {
        UserEntity user = userMapper.findByEmail(request.email())
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        return issueTokens(user.getUserId());
    }

    public TokenResponse reissue(String refreshToken) {
        if (!jwtProvider.isValid(refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtProvider.getUserId(refreshToken);
        String stored = redisTemplate.opsForValue().get(REFRESH_KEY + userId);

        if (!refreshToken.equals(stored)) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        return issueTokens(userId);
    }

    public void logout(Long userId) {
        redisTemplate.delete(REFRESH_KEY + userId);
    }

    private TokenResponse issueTokens(Long userId) {
        String accessToken = jwtProvider.createAccessToken(userId);
        String refreshToken = jwtProvider.createRefreshToken(userId);
        redisTemplate.opsForValue().set(REFRESH_KEY + userId, refreshToken, REFRESH_TTL);
        return TokenResponse.of(accessToken, refreshToken);
    }
}
