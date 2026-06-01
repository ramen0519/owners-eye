package com.ownerseye.ownerseye.global.event;

public record AnalysisCacheEvictEvent(Long userId, Long storeId, String yearMonth) {}
