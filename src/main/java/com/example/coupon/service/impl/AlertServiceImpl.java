package com.example.coupon.service.impl;

import com.example.coupon.mapper.AlertRecordMapper;
import com.example.coupon.model.entity.AlertRecord;
import com.example.coupon.model.enums.AlertType;
import com.example.coupon.service.AlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AlertServiceImpl implements AlertService {

    @Autowired
    private AlertRecordMapper alertRecordMapper;

    @Override
    public void alert(AlertType type, String bizId, String detail) {
        log.error("[ALERT] type={} bizId={} detail={}", type, bizId, detail);
        // 持久化告警记录，可在此扩展钉钉/邮件/短信通知
        AlertRecord record = AlertRecord.builder()
                .type(type.name())
                .bizId(bizId)
                .detail(detail)
                .status(0)
                .createTime(LocalDateTime.now())
                .build();
        try {
            alertRecordMapper.insert(record);
        } catch (Exception e) {
            log.error("告警记录写入失败", e);
        }
    }
}
