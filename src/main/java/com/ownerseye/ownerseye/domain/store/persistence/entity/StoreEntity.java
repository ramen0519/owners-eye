package com.ownerseye.ownerseye.domain.store.persistence.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreEntity {

    private Long storeId;
    private Long userId;
    private String storeName;
    private LocalDateTime createdAt;

    @Builder
    public StoreEntity(Long userId, String storeName) {
        this.userId = userId;
        this.storeName = storeName;
    }
}
