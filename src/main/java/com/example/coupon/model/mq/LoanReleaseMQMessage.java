package com.example.coupon.model.mq;

import lombok.Data;

@Data
public class LoanReleaseMQMessage {
    private Long userId;
    private Long activityId;
    private String loanOrderNo;
    private String bizId;
}
