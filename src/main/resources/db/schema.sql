CREATE DATABASE IF NOT EXISTS coupon_db DEFAULT CHARACTER SET utf8mb4;
USE coupon_db;

-- 券模板表
CREATE TABLE IF NOT EXISTS coupon (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)  NOT NULL,
    type        TINYINT       NOT NULL COMMENT '1=满减 2=折扣 3=无门槛',
    face_value  DECIMAL(10,2) NOT NULL,
    min_amount  DECIMAL(10,2) NOT NULL DEFAULT 0,
    valid_days  INT           NOT NULL COMMENT '领取后有效天数',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='券模板表';

-- 优惠券活动表
CREATE TABLE IF NOT EXISTS coupon_activity (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    activity_code   VARCHAR(64)  NOT NULL                COMMENT '活动标识，对应策略路由key',
    name            VARCHAR(100) NOT NULL,
    coupon_id       BIGINT       NOT NULL,
    stock_total     INT          NOT NULL,
    stock_used      INT          NOT NULL DEFAULT 0,
    valid_days      INT          NOT NULL DEFAULT 7      COMMENT '券有效天数',
    start_time      DATETIME     NOT NULL,
    end_time        DATETIME     NOT NULL,
    status          TINYINT      NOT NULL DEFAULT 0      COMMENT '0=未开始 1=进行中 2=已结束',
    warmup_status   TINYINT      NOT NULL DEFAULT 0      COMMENT '0=未预热 1=已预热',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_activity_code (activity_code),
    INDEX idx_status_time (status, start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券活动表';

-- 用户领券记录表
CREATE TABLE IF NOT EXISTS user_coupon (
    id            BIGINT   NOT NULL AUTO_INCREMENT,
    user_id       BIGINT   NOT NULL,
    activity_id   BIGINT   NOT NULL,
    coupon_id     BIGINT   NOT NULL,
    status        TINYINT  NOT NULL DEFAULT 0
                           COMMENT '0=ACTIVATED已激活 1=CLAIMED已领取 2=USED已使用 3=EXPIRED已过期',
    activate_time DATETIME NOT NULL                COMMENT '激活时间',
    claim_time    DATETIME                         COMMENT '翻转为已领取时间',
    expire_time   DATETIME NOT NULL,
    use_time      DATETIME,
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_activity (user_id, activity_id),
    INDEX idx_status_activate (status, activate_time),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户领券记录表';

-- 本地消息表（Outbox模式）
CREATE TABLE IF NOT EXISTS message_outbox (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    biz_id           VARCHAR(64)  NOT NULL               COMMENT 'userId_activityId，唯一',
    topic            VARCHAR(128) NOT NULL,
    tag              VARCHAR(64),
    payload          TEXT         NOT NULL,
    status           VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SENT/DEAD',
    retry_count      INT          NOT NULL DEFAULT 0,
    next_retry_time  DATETIME,
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_biz_id (biz_id),
    INDEX idx_status_retry (status, next_retry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='本地消息表';

-- 告警记录表
CREATE TABLE IF NOT EXISTS alert_record (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    type        VARCHAR(64)  NOT NULL COMMENT 'WARMUP_MISS/STOCK_INCONSISTENT/MQ_DEAD/OUTBOX_DEAD',
    biz_id      VARCHAR(64),
    detail      TEXT,
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0=待处理 1=已处理',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_type_status (type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警记录表';

-- 用户查额记录表
CREATE TABLE IF NOT EXISTS user_credit_check_record (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL,
    activity_id   BIGINT       NOT NULL,
    loan_order_no VARCHAR(64)           COMMENT '放款订单号',
    check_status  TINYINT      NOT NULL DEFAULT 0 COMMENT '0=未查额 1=已查额 2=已放款',
    check_time    DATETIME              COMMENT '查额时间',
    loan_time     DATETIME              COMMENT '放款时间',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_activity (user_id, activity_id),
    INDEX idx_loan_order (loan_order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户查额记录表';
