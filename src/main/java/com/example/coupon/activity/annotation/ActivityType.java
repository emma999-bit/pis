package com.example.coupon.activity.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ActivityType {
    /** 活动标识，与 coupon_activity.activity_code 对应 */
    String value();
}
