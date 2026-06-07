package com.example.coupon.service;

import com.example.coupon.model.entity.UserCreditCheckRecord;

public interface UserCreditCheckService {

    UserCreditCheckRecord getRecord(Long userId, Long activityId);

    /** 用户是否已完成查额（checkStatus >= 1） */
    boolean hasChecked(Long userId, Long activityId);
}
