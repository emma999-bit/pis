package com.example.coupon.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ActivityEnterDTO {

    @NotNull
    private Long userId;

    @NotNull
    private Long activityId;
}
