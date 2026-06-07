package com.example.coupon.model.enums;

public enum MessageStatus {

    PENDING("PENDING", "Pending send"),
    SENT("SENT", "Successfully sent"),
    DEAD("DEAD", "Dead letter - max retries exceeded");

    private final String code;
    private final String desc;

    MessageStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
