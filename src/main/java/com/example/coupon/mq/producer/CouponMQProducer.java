package com.example.coupon.mq.producer;

import com.alibaba.fastjson2.JSON;
import com.example.coupon.model.mq.CouponMQMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CouponMQProducer {

    public static final String TOPIC = "COUPON_TOPIC";
    public static final String TAG = "CLAIM";
    public static final String DESTINATION = TOPIC + ":" + TAG;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 同步发送，3秒超时；抛出异常表示发送失败
     */
    public void syncSend(CouponMQMessage message) {
        String payload = JSON.toJSONString(message);
        log.info("发送领券MQ bizId={}", message.getBizId());
        rocketMQTemplate.syncSend(DESTINATION,
                MessageBuilder.withPayload(payload).build(), 3000);
    }
}
