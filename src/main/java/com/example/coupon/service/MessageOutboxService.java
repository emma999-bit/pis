package com.example.coupon.service;

import com.example.coupon.model.entity.MessageOutbox;

import java.util.List;

public interface MessageOutboxService {

    /** 写 Outbox 并同步发送 MQ；发送失败时保持 PENDING，由定时任务重试 */
    void saveAndSend(Long activityId, Long userId, Long couponId);

    /** 定时任务调用：扫描 PENDING 记录重试 */
    List<MessageOutbox> scanPending(int limit);

    /** 重试单条记录 */
    void retry(MessageOutbox outbox);
}
