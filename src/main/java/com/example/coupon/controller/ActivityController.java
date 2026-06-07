package com.example.coupon.controller;

import com.example.coupon.activity.ActivityHandlerFactory;
import com.example.coupon.common.Result;
import com.example.coupon.model.dto.ActivityEnterDTO;
import com.example.coupon.model.dto.ClaimCouponDTO;
import com.example.coupon.model.vo.ActivityLandingVO;
import com.example.coupon.model.vo.ClaimCouponVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 活动落地页统一入口。
 * 新增活动类型只需新建 ActivityHandler 实现类 + @ActivityType，此处代码永远不变。
 */
@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Autowired
    private ActivityHandlerFactory factory;

    /** 落地页：资格校验 + 状态查询 */
    @PostMapping("/{activityCode}/enter")
    public Result<ActivityLandingVO> enter(@PathVariable String activityCode,
                                           @Valid @RequestBody ActivityEnterDTO dto) {
        ActivityLandingVO vo = factory.getHandler(activityCode).enter(dto);
        return Result.ok(vo);
    }

    /** 用户主动领券 */
    @PostMapping("/{activityCode}/claim")
    public Result<ClaimCouponVO> claim(@PathVariable String activityCode,
                                       @Valid @RequestBody ClaimCouponDTO dto) {
        ClaimCouponVO vo = factory.getHandler(activityCode).claimCoupon(dto);
        return Result.ok(vo);
    }
}
