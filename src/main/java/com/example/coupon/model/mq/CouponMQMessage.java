package com.example.coupon.model.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponMQMessage {

    private Long userId;

    private Long activityId;

    private Long couponId;

    private String bizId;
}
