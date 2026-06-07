package com.example.coupon.controller;

import com.example.coupon.common.Result;
import com.example.coupon.service.WarmupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 运营手动触发库存预热 */
@RestController
@RequestMapping("/coupon/warmup")
public class StockWarmupController {

    @Autowired
    private WarmupService warmupService;

    @PostMapping("/{activityId}")
    public Result<Void> warmup(@PathVariable Long activityId) {
        warmupService.warmup(activityId);
        return Result.ok("预热完成", null);
    }
}
