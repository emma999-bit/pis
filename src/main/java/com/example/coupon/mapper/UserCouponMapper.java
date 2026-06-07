package com.example.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.coupon.model.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

    /**
     * Check if user has already claimed a coupon for the given activity.
     *
     * @param userId     the user ID
     * @param activityId the activity ID
     * @return existing user coupon or null
     */
    @Select("SELECT * FROM user_coupon WHERE user_id = #{userId} AND activity_id = #{activityId} LIMIT 1")
    UserCoupon findByUserIdAndActivityId(@Param("userId") Long userId, @Param("activityId") Long activityId);

    /**
     * Insert user coupon with IGNORE to handle concurrent duplicates gracefully.
     *
     * @param userCoupon the user coupon to insert
     * @return affected rows
     */
    int insertIgnore(UserCoupon userCoupon);

    /** 批量翻转 ACTIVATED→CLAIMED，返回影响行数 */
    int flipActivatedToClaimed();
}
