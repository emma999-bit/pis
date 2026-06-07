package com.example.coupon.model.enums;

public enum AlertType {

    WARMUP_MISS("WARMUP_MISS", "Redis cache miss - stock was warmed up from DB"),
    STOCK_INCONSISTENT("STOCK_INCONSISTENT", "DB stock inconsistency detected during consumer processing"),
    MQ_DEAD("MQ_DEAD", "MQ message reached dead letter state"),
    OUTBOX_DEAD("OUTBOX_DEAD", "Outbox message reached dead letter after max retries");

    private final String code;
    private final String desc;

    AlertType(String code, String desc) {
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
