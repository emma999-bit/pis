package com.example.coupon.activity.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.coupon.activity.ActivityHandler;
import com.example.coupon.activity.annotation.ActivityType;
import com.example.coupon.common.BizException;
import com.example.coupon.mapper.CouponActivityMapper;
import com.example.coupon.mapper.UserCouponMapper;
import com.example.coupon.model.dto.ActivityEnterDTO;
import com.example.coupon.model.dto.ClaimCouponDTO;
import com.example.coupon.model.entity.CouponActivity;
import com.example.coupon.model.entity.UserCoupon;
import com.example.coupon.model.vo.ActivityLandingVO;
import com.example.coupon.model.vo.ClaimCouponVO;
import com.example.coupon.service.CouponService;
import com.example.coupon.service.UserCreditCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

/**
 * 查额专项活动处理器
 * 新增活动类型时，只需新建类 + @ActivityType("新标识")，核心调度代码零改动。
 */
@Slf4j
@ActivityType("CREDIT_CHECK")
public class CreditCheckActivityHandler implements ActivityHandler {

    public static final String ACTIVITY_CODE = "CREDIT_CHECK";

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserCreditCheckService creditCheckService;

    @Autowired
    private CouponActivityMapper activityMapper;

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Override
    public ActivityLandingVO enter(ActivityEnterDTO dto) {
        CouponActivity activity = getActiveActivity(dto.getActivityId());

        // 查询用户查额状态
        var checkRecord = creditCheckService.getRecord(dto.getUserId(), dto.getActivityId());
        int checkStatus = checkRecord == null ? 0 : checkRecord.getCheckStatus();

        // 查询用户领券记录
        UserCoupon userCoupon = userCouponMapper.selectOne(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, dto.getUserId())
                .eq(UserCoupon::getActivityId, dto.getActivityId()));

        Integer couponStatus = userCoupon == null ? null : userCoupon.getStatus();
        String statusDesc = resolveCouponStatusDesc(couponStatus);

        // 已查额且未领取 → 可领券
        boolean canClaim = checkStatus >= 1 && couponStatus == null;

        return ActivityLandingVO.builder()
                .activityId(activity.getId())
                .activityName(activity.getName())
                .creditCheckStatus(checkStatus)
                .couponStatus(couponStatus)
                .couponStatusDesc(statusDesc)
                .canClaim(canClaim)
                .build();
    }

    @Override
    public ClaimCouponVO claimCoupon(ClaimCouponDTO dto) {
        // 资格校验：用户必须已完成查额
        if (!creditCheckService.hasChecked(dto.getUserId(), dto.getActivityId())) {
            return ClaimCouponVO.builder()
                    .userId(dto.getUserId())
                    .activityId(dto.getActivityId())
                    .success(false)
                    .message("请先完成查额操作").build();
        }
        // 活动有效性校验
        getActiveActivity(dto.getActivityId());
        // 委托核心领券流程
        return couponService.claimCoupon(dto);
    }

    @Override
    public String getActivityCode() {
        return ACTIVITY_CODE;
    }

    private CouponActivity getActiveActivity(Long activityId) {
        CouponActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BizException(404, "活动不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (activity.getStatus() != 1
                || now.isBefore(activity.getStartTime())
                || now.isAfter(activity.getEndTime())) {
            throw new BizException(400, "活动未在进行中");
        }
        return activity;
    }

    private String resolveCouponStatusDesc(Integer status) {
        if (status == null) return "未领取";
        switch (status) {
            case 0: return "已激活";
            case 1: return "已领取";
            case 2: return "已使用";
            case 3: return "已过期";
            default: return "未知";
        }
    }
}
