package com.ownerseye.ownerseye.global.event;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AnalysisCacheEvictListener {

    private final CacheManager cacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AnalysisCacheEvictEvent event) {
        String key = event.userId() + ":" + event.storeId() + ":" + event.yearMonth();
        var cache = cacheManager.getCache("analysis");
        if (cache != null) cache.evict(key);
    }
}
