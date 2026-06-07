package com.example.coupon.service.scheduler;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.coupon.mapper.MessageOutboxMapper;
import com.example.coupon.model.entity.MessageOutbox;
import com.example.coupon.model.enums.AlertType;
import com.example.coupon.model.enums.MessageStatus;
import com.example.coupon.service.AlertService;
import com.example.coupon.service.MessageOutboxService;
import com.example.coupon.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OutboxRetryScheduler {

    private static final int MAX_RETRY = 5;

    @Autowired
    private MessageOutboxService outboxService;

    @Autowired
    private MessageOutboxMapper outboxMapper;

    @Autowired
    private AlertService alertService;

    @Autowired
    private StockService stockService;

    @Scheduled(fixedDelay = 60_000)
    public void retry() {
        List<MessageOutbox> pendings = outboxService.scanPending(100);
        if (pendings.isEmpty()) return;

        log.info("Outbox定时重试，本批数量={}", pendings.size());
        for (MessageOutbox outbox : pendings) {
            outboxService.retry(outbox);
        }

        // 扫描超过5次仍失败的记录，标记DEAD并告警
        handleDead();
    }

    private void handleDead() {
        List<MessageOutbox> deadList = outboxMapper.selectDeadCandidates(MAX_RETRY);
        for (MessageOutbox outbox : deadList) {
            outboxMapper.update(null, new LambdaUpdateWrapper<MessageOutbox>()
                    .eq(MessageOutbox::getId, outbox.getId())
                    .set(MessageOutbox::getStatus, MessageStatus.DEAD.name())
                    .set(MessageOutbox::getUpdateTime, LocalDateTime.now()));

            // 解析 bizId = userId_activityId，回滚 Redis 库存
            String[] parts = outbox.getBizId().split("_");
            if (parts.length == 2) {
                try {
                    Long userId = Long.parseLong(parts[0]);
                    Long activityId = Long.parseLong(parts[1]);
                    stockService.rollbackStock(activityId, userId);
                } catch (NumberFormatException e) {
                    log.error("bizId格式异常，无法回滚 bizId={}", outbox.getBizId());
                }
            }
            alertService.alert(AlertType.OUTBOX_DEAD, outbox.getBizId(),
                    "Outbox超过" + MAX_RETRY + "次重试仍失败，需人工处理");
        }
    }
}
