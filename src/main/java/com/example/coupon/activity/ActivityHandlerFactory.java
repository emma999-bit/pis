package com.example.coupon.activity;

import com.example.coupon.activity.annotation.ActivityType;
import com.example.coupon.common.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ActivityHandlerFactory implements ApplicationContextAware {

    /** 路由表：activityCode → ActivityHandler */
    private final Map<String, ActivityHandler> handlerMap = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        Map<String, Object> beans = ctx.getBeansWithAnnotation(ActivityType.class);
        beans.forEach((beanName, bean) -> {
            if (!(bean instanceof ActivityHandler)) {
                log.warn("Bean {} 标注了 @ActivityType 但未实现 ActivityHandler，已跳过", beanName);
                return;
            }
            ActivityType annotation = bean.getClass().getAnnotation(ActivityType.class);
            handlerMap.put(annotation.value(), (ActivityHandler) bean);
            log.info("注册活动处理器: {} -> {}", annotation.value(), bean.getClass().getSimpleName());
        });
    }

    public ActivityHandler getHandler(String activityCode) {
        ActivityHandler handler = handlerMap.get(activityCode);
        if (handler == null) {
            throw new BizException(404, "未找到活动处理器: " + activityCode);
        }
        return handler;
    }
}
