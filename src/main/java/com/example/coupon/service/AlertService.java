package com.example.coupon.service;

import com.example.coupon.model.enums.AlertType;

public interface AlertService {

    void alert(AlertType type, String bizId, String detail);
}
