package com.example.coupon.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimCouponVO {

    private Long userId;

    private Long activityId;

    private Long couponId;

    private String message;

    private Boolean success;
}
