package com.example.coupon.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActivityLandingVO {

    private Long activityId;
    private String activityName;
    /** 0=未查额 1=已查额 2=已放款 */
    private Integer creditCheckStatus;
    /** 0=ACTIVATED 1=CLAIMED 2=USED 3=EXPIRED null=未领取 */
    private Integer couponStatus;
    private String couponStatusDesc;
    /** 是否可以领券 */
    private Boolean canClaim;
}
