package com.example.coupon.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("coupon_activity")
public class CouponActivity {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 活动标识，对应策略路由key，如 CREDIT_CHECK */
    private String activityCode;
    private String name;
    private Long couponId;
    private Integer stockTotal;
    private Integer stockUsed;
    private Integer status;
    private Integer warmupStatus;
    /** 券有效天数（领取后） */
    private Integer validDays;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
