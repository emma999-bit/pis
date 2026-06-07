package com.example.coupon.service.impl;

import com.example.coupon.mapper.CouponActivityMapper;
import com.example.coupon.model.entity.CouponActivity;
import com.example.coupon.model.enums.AlertType;
import com.example.coupon.service.AlertService;
import com.example.coupon.service.StockService;
import com.example.coupon.service.WarmupService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WarmupServiceImpl implements WarmupService {

    @Autowired
    private CouponActivityMapper activityMapper;

    @Autowired
    private StockService stockService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private AlertService alertService;

    @Override
    public void warmup(Long activityId) {
        String lockKey = StockService.WARMUP_LOCK_KEY_PREFIX + activityId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            // waitTime=0：拿不到锁直接返回，避免重复预热
            acquired = lock.tryLock(0, 30, TimeUnit.SECONDS);
            if (!acquired) {
                log.info("其他线程正在预热 activityId={}，本次跳过", activityId);
                return;
            }
            CouponActivity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                log.warn("活动不存在 activityId={}", activityId);
                return;
            }
            int available = activity.getStockTotal() - activity.getStockUsed();
            if (available <= 0) {
                log.warn("活动库存已耗尽 activityId={}", activityId);
                return;
            }
            stockService.initStock(activityId, available);
            // 触发告警：预热缺失，自动补救
            alertService.alert(AlertType.WARMUP_MISS, String.valueOf(activityId),
                    "活动预热缺失，已自动从DB加载库存: " + available);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("预热加锁被中断 activityId={}", activityId, e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
