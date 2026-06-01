package com.ownerseye.ownerseye.domain.insight.domain.tools;

public class InsightContext {

    private static final ThreadLocal<Long> userId = new ThreadLocal<>();
    private static final ThreadLocal<Long> storeId = new ThreadLocal<>();
    private static final ThreadLocal<Integer> callCount = ThreadLocal.withInitial(() -> 0);

    public static void set(Long userId, Long storeId) {
        InsightContext.userId.set(userId);
        InsightContext.storeId.set(storeId);
        InsightContext.callCount.set(0);
    }

    public static Long getUserId() {
        return userId.get();
    }

    public static Long getStoreId() {
        return storeId.get();
    }

    public static int incrementCallCount() {
        int count = callCount.get() + 1;
        callCount.set(count);
        return count;
    }

    public static void clear() {
        userId.remove();
        storeId.remove();
        callCount.remove();
    }
}
