package com.ownerseye.ownerseye.domain.auth.persistence.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {

    private Long userId;
    private String email;
    private String password;
    private String name;
    private LocalDateTime createdAt;

    @Builder
    public UserEntity(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }
}
