package com.example.coupon.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ClaimCouponDTO {

    @NotNull(message = "userId cannot be null")
    private Long userId;

    @NotNull(message = "activityId cannot be null")
    private Long activityId;
}
