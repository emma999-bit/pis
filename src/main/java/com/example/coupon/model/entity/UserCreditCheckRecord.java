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
@TableName("user_credit_check_record")
public class UserCreditCheckRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long activityId;
    private String loanOrderNo;
    /** 0=未查额 1=已查额 2=已放款 */
    private Integer checkStatus;
    private LocalDateTime checkTime;
    private LocalDateTime loanTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
