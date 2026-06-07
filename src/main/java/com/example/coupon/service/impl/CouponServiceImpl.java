package com.example.coupon.service.impl;

import com.example.coupon.common.BizException;
import com.example.coupon.model.dto.ClaimCouponDTO;
import com.example.coupon.model.entity.CouponActivity;
import com.example.coupon.model.vo.ClaimCouponVO;
import com.example.coupon.mapper.CouponActivityMapper;
import com.example.coupon.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private StockService stockService;

    @Autowired
    private WarmupService warmupService;

    @Autowired
    private MessageOutboxService messageOutboxService;

    @Autowired
    private CouponActivityMapper activityMapper;

    @Override
    public ClaimCouponVO claimCoupon(ClaimCouponDTO dto) {
        Long userId = dto.getUserId();
        Long activityId = dto.getActivityId();

        // 步骤2：Lua 原子扣减
        int result = stockService.deductStock(activityId, userId);

        // key 不存在 → 自动预热 → 重试一次
        if (result == StockService.RESULT_KEY_MISSING) {
            log.warn("Redis key缺失，触发自动预热 activityId={}", activityId);
            warmupService.warmup(activityId);
            result = stockService.deductStock(activityId, userId);
        }

        switch (result) {
            case StockService.RESULT_ALREADY_CLAIMED:
                return ClaimCouponVO.builder()
                        .userId(userId).activityId(activityId)
                        .success(false).message("您已领取过该优惠券").build();

            case StockService.RESULT_OUT_OF_STOCK:
                return ClaimCouponVO.builder()
                        .userId(userId).activityId(activityId)
                        .success(false).message("券已抢完").build();

            case StockService.RESULT_KEY_MISSING:
                return ClaimCouponVO.builder()
                        .userId(userId).activityId(activityId)
                        .success(false).message("系统繁忙，请稍后重试").build();

            case StockService.RESULT_SUCCESS:
                return doClaimAfterStockDeducted(userId, activityId);

            default:
                throw new BizException("未知的库存扣减结果: " + result);
        }
    }

    private ClaimCouponVO doClaimAfterStockDeducted(Long userId, Long activityId) {
        CouponActivity activity = activityMapper.selectById(activityId);
        Long couponId = activity.getCouponId();

        // 步骤3+4：写 Outbox + 发 MQ
        try {
            messageOutboxService.saveAndSend(activityId, userId, couponId);
        } catch (Exception e) {
            // 步骤3写入失败：Redis 回滚
            log.error("Outbox写入失败，回滚Redis库存 userId={} activityId={}", userId, activityId, e);
            stockService.rollbackStock(activityId, userId);
            return ClaimCouponVO.builder()
                    .userId(userId).activityId(activityId)
                    .success(false).message("领券失败，请重试").build();
        }

        return ClaimCouponVO.builder()
                .userId(userId).activityId(activityId)
                .couponId(couponId)
                .success(true).message("领券成功，发放中").build();
    }
}
