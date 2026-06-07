package com.example.coupon.service;

public interface WarmupService {

    /**
     * Warm up Redis stock cache for the given activity from the database.
     * Uses Redisson distributed lock to prevent thundering herd.
     *
     * @param activityId the coupon activity ID
     */
    void warmup(Long activityId);
}
