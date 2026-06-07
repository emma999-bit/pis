package com.example.coupon.service.scheduler;

import com.example.coupon.mapper.UserCouponMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CouponStatusFlipScheduler {

    @Autowired
    private UserCouponMapper userCouponMapper;

    /**
     * 每分钟扫描 ACTIVATED 状态的券，批量翻转为 CLAIMED。
     * 翻转条件：activate_time 早于当前时间（可按业务配置延迟时长）。
     */
    @Scheduled(fixedDelay = 60_000)
    public void flip() {
        int affected = userCouponMapper.flipActivatedToClaimed();
        if (affected > 0) {
            log.info("定时任务翻转券状态 ACTIVATED→CLAIMED 数量={}", affected);
        }
    }
}
