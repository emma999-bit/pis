package com.example.coupon.mq.consumer;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.coupon.mapper.UserCouponMapper;
import com.example.coupon.model.dto.ClaimCouponDTO;
import com.example.coupon.model.entity.UserCoupon;
import com.example.coupon.model.mq.LoanReleaseMQMessage;
import com.example.coupon.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 核算放款MQ消费者。
 * 放款后触发：若用户已有ACTIVATED券 → 翻转为CLAIMED；否则自动发券。
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "LOAN_RELEASE_TOPIC",
        selectorExpression = "CREDIT_CHECK",
        consumerGroup = "loan-release-coupon-group"
)
public class LoanReleaseConsumer implements RocketMQListener<String> {

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Autowired
    private CouponService couponService;

    @Override
    public void onMessage(String message) {
        LoanReleaseMQMessage msg = JSON.parseObject(message, LoanReleaseMQMessage.class);
        log.info("收到放款MQ userId={} activityId={}", msg.getUserId(), msg.getActivityId());

        // 幂等：查询已存在领券记录
        UserCoupon existing = userCouponMapper.selectOne(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, msg.getUserId())
                .eq(UserCoupon::getActivityId, msg.getActivityId()));

        if (existing != null) {
            if (existing.getStatus() == 0) {
                // ACTIVATED → 翻转为 CLAIMED
                userCouponMapper.update(null, new LambdaUpdateWrapper<UserCoupon>()
                        .eq(UserCoupon::getId, existing.getId())
                        .eq(UserCoupon::getStatus, 0)  // 防并发重复翻转
                        .set(UserCoupon::getStatus, 1)
                        .set(UserCoupon::getClaimTime, LocalDateTime.now()));
                log.info("放款触发券状态翻转 ACTIVATED→CLAIMED userId={}", msg.getUserId());
            } else {
                log.info("幂等跳过，券状态={} userId={}", existing.getStatus(), msg.getUserId());
            }
            return;
        }

        // 用户尚未领券，触发自动发券
        log.info("放款触发自动发券 userId={} activityId={}", msg.getUserId(), msg.getActivityId());
        ClaimCouponDTO dto = new ClaimCouponDTO();
        dto.setUserId(msg.getUserId());
        dto.setActivityId(msg.getActivityId());
        couponService.claimCoupon(dto);
    }
}
