package com.example.coupon.service;

import com.example.coupon.model.dto.ClaimCouponDTO;
import com.example.coupon.model.vo.ClaimCouponVO;

public interface CouponService {

    /**
     * Claim a coupon for the given user and activity.
     *
     * @param dto the claim coupon request DTO
     * @return the claim coupon result VO
     */
    ClaimCouponVO claimCoupon(ClaimCouponDTO dto);
}
