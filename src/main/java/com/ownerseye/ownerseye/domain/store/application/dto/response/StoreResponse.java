package com.ownerseye.ownerseye.domain.store.application.dto.response;

import com.ownerseye.ownerseye.domain.store.persistence.entity.StoreEntity;

import java.time.LocalDateTime;

public record StoreResponse(
        Long storeId,
        String storeName,
        LocalDateTime createdAt
) {
    public static StoreResponse from(StoreEntity entity) {
        return new StoreResponse(
                entity.getStoreId(),
                entity.getStoreName(),
                entity.getCreatedAt()
        );
    }
}
