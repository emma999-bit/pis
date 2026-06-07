package com.example.coupon.service.impl;

import com.example.coupon.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;

@Slf4j
@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    @Qualifier("couponClaimScript")
    private DefaultRedisScript<Long> couponClaimScript;

    @Override
    public int deductStock(Long activityId, Long userId) {
        String stockKey = STOCK_KEY_PREFIX + activityId;
        String claimedKey = CLAIMED_KEY_PREFIX + activityId;
        Long result = redisTemplate.execute(
                couponClaimScript,
                Arrays.asList(stockKey, claimedKey),
                String.valueOf(userId)
        );
        return result == null ? RESULT_KEY_MISSING : result.intValue();
    }

    @Override
    public void rollbackStock(Long activityId, Long userId) {
        String stockKey = STOCK_KEY_PREFIX + activityId;
        String claimedKey = CLAIMED_KEY_PREFIX + activityId;
        redisTemplate.opsForValue().increment(stockKey);
        redisTemplate.opsForSet().remove(claimedKey, String.valueOf(userId));
        log.info("Redis库存回滚完成 activityId={} userId={}", activityId, userId);
    }

    @Override
    public void initStock(Long activityId, int stock) {
        String stockKey = STOCK_KEY_PREFIX + activityId;
        // 24小时TTL，活动结束后自然过期
        redisTemplate.opsForValue().set(stockKey, String.valueOf(stock), Duration.ofHours(24));
        log.info("Redis库存初始化完成 activityId={} stock={}", activityId, stock);
    }
}
