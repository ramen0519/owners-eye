package com.ownerseye.ownerseye.domain.store.application.service;

import com.ownerseye.ownerseye.domain.store.application.dto.request.StoreSaveRequest;
import com.ownerseye.ownerseye.domain.store.application.dto.response.StoreResponse;
import com.ownerseye.ownerseye.domain.store.exception.StoreException;
import com.ownerseye.ownerseye.domain.store.exception.code.StoreErrorCode;
import com.ownerseye.ownerseye.domain.store.persistence.entity.StoreEntity;
import com.ownerseye.ownerseye.domain.store.persistence.mapper.StoreMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreService {

    private final StoreMapper storeMapper;

    @Transactional
    public Long save(Long userId, StoreSaveRequest request) {
        StoreEntity store = StoreEntity.builder()
                .userId(userId)
                .storeName(request.storeName())
                .build();

        storeMapper.save(store);
        return store.getStoreId();
    }

    public List<StoreResponse> findAllByUserId(Long userId) {
        return storeMapper.findAllByUserId(userId).stream()
                .map(StoreResponse::from)
                .toList();
    }

    @Transactional
    public void delete(Long userId, Long storeId) {
        storeMapper.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        storeMapper.delete(storeId);
    }
}
