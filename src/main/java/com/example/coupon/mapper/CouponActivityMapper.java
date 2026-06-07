package com.example.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.coupon.model.entity.CouponActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CouponActivityMapper extends BaseMapper<CouponActivity> {

    /**
     * Atomically increment stock_used by 1 only if stock is available and activity is active.
     *
     * @param activityId the activity ID
     * @return affected rows (1 = success, 0 = no stock or inactive)
     */
    @Update("UPDATE coupon_activity SET stock_used = stock_used + 1 " +
            "WHERE id = #{activityId} AND (stock_total - stock_used) > 0 AND status = 1")
    int incrementStockUsed(@Param("activityId") Long activityId);
}
