package com.example.coupon.service;

public interface StockService {

    /**
     * Redis key for available stock: coupon:stock:{activityId}
     */
    String STOCK_KEY_PREFIX = "coupon:stock:";

    /**
     * Redis key for claimed user set: coupon:claimed:{activityId}
     */
    String CLAIMED_KEY_PREFIX = "coupon:claimed:";

    /**
     * Redis key for warmup distributed lock: coupon:warmup:lock:{activityId}
     */
    String WARMUP_LOCK_KEY_PREFIX = "coupon:warmup:lock:";

    /**
     * Lua result: already claimed
     */
    int RESULT_ALREADY_CLAIMED = -2;

    /**
     * Lua result: redis key missing (cache not warmed up)
     */
    int RESULT_KEY_MISSING = -1;

    /**
     * Lua result: out of stock
     */
    int RESULT_OUT_OF_STOCK = 0;

    /**
     * Lua result: success
     */
    int RESULT_SUCCESS = 1;

    /**
     * Deduct stock from Redis using atomic Lua script.
     * Returns one of RESULT_* constants.
     *
     * @param activityId the coupon activity ID
     * @param userId     the user ID claiming the coupon
     * @return result code
     */
    int deductStock(Long activityId, Long userId);

    /**
     * Rollback stock in Redis: INCR stock key and SREM from claimed set.
     *
     * @param activityId the coupon activity ID
     * @param userId     the user ID
     */
    void rollbackStock(Long activityId, Long userId);

    /**
     * Initialize Redis stock from a given count (called during warmup).
     *
     * @param activityId the coupon activity ID
     * @param stock      available stock count
     */
    void initStock(Long activityId, int stock);
}
