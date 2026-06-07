package com.example.coupon.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.coupon.mapper.MessageOutboxMapper;
import com.example.coupon.model.entity.MessageOutbox;
import com.example.coupon.model.enums.MessageStatus;
import com.example.coupon.model.mq.CouponMQMessage;
import com.example.coupon.mq.producer.CouponMQProducer;
import com.example.coupon.service.MessageOutboxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MessageOutboxServiceImpl implements MessageOutboxService {

    private static final int MAX_RETRY = 5;

    @Autowired
    private MessageOutboxMapper outboxMapper;

    @Autowired
    private CouponMQProducer producer;

    @Override
    public void saveAndSend(Long activityId, Long userId, Long couponId) {
        String bizId = userId + "_" + activityId;
        CouponMQMessage mqMsg = CouponMQMessage.builder()
                .userId(userId).activityId(activityId)
                .couponId(couponId).bizId(bizId).build();

        MessageOutbox outbox = MessageOutbox.builder()
                .bizId(bizId)
                .topic(CouponMQProducer.TOPIC)
                .tag(CouponMQProducer.TAG)
                .payload(JSON.toJSONString(mqMsg))
                .status(MessageStatus.PENDING.name())
                .retryCount(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        outboxMapper.insert(outbox);

        try {
            producer.syncSend(mqMsg);
            outboxMapper.update(null, new LambdaUpdateWrapper<MessageOutbox>()
                    .eq(MessageOutbox::getBizId, bizId)
                    .set(MessageOutbox::getStatus, MessageStatus.SENT.name())
                    .set(MessageOutbox::getUpdateTime, LocalDateTime.now()));
            log.info("MQ发送成功 bizId={}", bizId);
        } catch (Exception e) {
            log.warn("MQ发送失败，保持PENDING等待定时重试 bizId={}", bizId, e);
            // 不抛异常，由定时任务重试
        }
    }

    @Override
    public List<MessageOutbox> scanPending(int limit) {
        return outboxMapper.selectList(new LambdaQueryWrapper<MessageOutbox>()
                .eq(MessageOutbox::getStatus, MessageStatus.PENDING.name())
                .lt(MessageOutbox::getRetryCount, MAX_RETRY)
                .le(MessageOutbox::getNextRetryTime, LocalDateTime.now())
                .last("LIMIT " + limit));
    }

    @Override
    public void retry(MessageOutbox outbox) {
        try {
            CouponMQMessage mqMsg = JSON.parseObject(outbox.getPayload(), CouponMQMessage.class);
            producer.syncSend(mqMsg);
            outboxMapper.update(null, new LambdaUpdateWrapper<MessageOutbox>()
                    .eq(MessageOutbox::getId, outbox.getId())
                    .set(MessageOutbox::getStatus, MessageStatus.SENT.name())
                    .set(MessageOutbox::getUpdateTime, LocalDateTime.now()));
            log.info("Outbox重试成功 bizId={}", outbox.getBizId());
        } catch (Exception e) {
            int nextCount = outbox.getRetryCount() + 1;
            // 指数退避：2^retryCount 分钟后重试
            LocalDateTime nextRetry = LocalDateTime.now().plusMinutes((long) Math.pow(2, nextCount));
            outboxMapper.update(null, new LambdaUpdateWrapper<MessageOutbox>()
                    .eq(MessageOutbox::getId, outbox.getId())
                    .set(MessageOutbox::getRetryCount, nextCount)
                    .set(MessageOutbox::getNextRetryTime, nextRetry)
                    .set(MessageOutbox::getUpdateTime, LocalDateTime.now()));
            log.warn("Outbox重试失败 bizId={} retryCount={}", outbox.getBizId(), nextCount);
        }
    }
}
