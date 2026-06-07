package com.example.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.coupon.mapper.UserCreditCheckRecordMapper;
import com.example.coupon.model.entity.UserCreditCheckRecord;
import com.example.coupon.service.UserCreditCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserCreditCheckServiceImpl implements UserCreditCheckService {

    @Autowired
    private UserCreditCheckRecordMapper recordMapper;

    @Override
    public UserCreditCheckRecord getRecord(Long userId, Long activityId) {
        return recordMapper.selectOne(new LambdaQueryWrapper<UserCreditCheckRecord>()
                .eq(UserCreditCheckRecord::getUserId, userId)
                .eq(UserCreditCheckRecord::getActivityId, activityId));
    }

    @Override
    public boolean hasChecked(Long userId, Long activityId) {
        UserCreditCheckRecord record = getRecord(userId, activityId);
        return record != null && record.getCheckStatus() >= 1;
    }
}
