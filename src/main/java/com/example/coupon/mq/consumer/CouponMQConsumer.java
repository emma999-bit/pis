package com.example.coupon.mq.consumer;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.coupon.mapper.CouponActivityMapper;
import com.example.coupon.mapper.UserCouponMapper;
import com.example.coupon.model.entity.CouponActivity;
import com.example.coupon.model.entity.UserCoupon;
import com.example.coupon.model.enums.AlertType;
import com.example.coupon.model.mq.CouponMQMessage;
import com.example.coupon.mq.producer.CouponMQProducer;
import com.example.coupon.service.AlertService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = CouponMQProducer.TOPIC,
        selectorExpression = CouponMQProducer.TAG,
        consumerGroup = "coupon-consumer-group"
)
public class CouponMQConsumer implements RocketMQListener<String> {

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Autowired
    private CouponActivityMapper activityMapper;

    @Autowired
    private AlertService alertService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(String message) {
        CouponMQMessage msg = JSON.parseObject(message, CouponMQMessage.class);
        log.info("收到领券MQ bizId={}", msg.getBizId());

        // 步骤6：幂等校验
        Long exists = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, msg.getUserId())
                .eq(UserCoupon::getActivityId, msg.getActivityId()));
        if (exists > 0) {
            log.info("幂等跳过，已存在领券记录 bizId={}", msg.getBizId());
            return;
        }

        // 步骤7：DB兜底扣库存
        int affected = activityMapper.deductStock(msg.getActivityId());
        if (affected == 0) {
            // DB与Redis不一致，告警后ACK，不触发重试
            log.error("DB扣库存影响行数=0，Redis与DB不一致 bizId={}", msg.getBizId());
            alertService.alert(AlertType.STOCK_INCONSISTENT, msg.getBizId(),
                    "DB扣库存失败，activityId=" + msg.getActivityId());
            return;
        }

        // 步骤7：写领券记录（INSERT IGNORE 幂等兜底）
        CouponActivity activity = activityMapper.selectById(msg.getActivityId());
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(msg.getUserId())
                .activityId(msg.getActivityId())
                .couponId(msg.getCouponId())
                .status(0)  // ACTIVATED
                .activateTime(LocalDateTime.now())
                .expireTime(LocalDateTime.now().plusDays(activity.getValidDays()))
                .createTime(LocalDateTime.now())
                .build();
        userCouponMapper.insertIgnore(userCoupon);
        log.info("领券记录写入成功 bizId={}", msg.getBizId());
    }
}
