package com.example.coupon.activity;

import com.example.coupon.model.dto.ActivityEnterDTO;
import com.example.coupon.model.dto.ClaimCouponDTO;
import com.example.coupon.model.vo.ActivityLandingVO;
import com.example.coupon.model.vo.ClaimCouponVO;

public interface ActivityHandler {

    /** 落地页入口：资格校验 + 状态查询 */
    ActivityLandingVO enter(ActivityEnterDTO dto);

    /** 用户主动领券 */
    ClaimCouponVO claimCoupon(ClaimCouponDTO dto);

    /** 活动标识，与 @ActivityType.value() 对应 */
    String getActivityCode();
}
