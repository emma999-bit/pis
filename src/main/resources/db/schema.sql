CREATE DATABASE IF NOT EXISTS coupon_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE coupon_db;

-- Coupon activity table
CREATE TABLE IF NOT EXISTS coupon_activity (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    name VARCHAR(128) NOT NULL COMMENT 'Activity name',
    stock_total INT NOT NULL DEFAULT 0 COMMENT 'Total stock',
    stock_used INT NOT NULL DEFAULT 0 COMMENT 'Used stock',
    status TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 1=active, 0=inactive',
    start_time DATETIME NOT NULL COMMENT 'Activity start time',
    end_time DATETIME NOT NULL COMMENT 'Activity end time',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Coupon activity';

-- Coupon table
CREATE TABLE IF NOT EXISTS coupon (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    activity_id BIGINT NOT NULL COMMENT 'Activity ID',
    code VARCHAR(64) NOT NULL COMMENT 'Coupon code',
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT 'Discount amount',
    status TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 1=available, 0=disabled',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    PRIMARY KEY (id),
    KEY idx_activity_id (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Coupon';

-- User coupon table
CREATE TABLE IF NOT EXISTS user_coupon (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    user_id BIGINT NOT NULL COMMENT 'User ID',
    activity_id BIGINT NOT NULL COMMENT 'Activity ID',
    coupon_id BIGINT NOT NULL COMMENT 'Coupon ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 1=valid, 0=used',
    claimed_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Claimed time',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_activity (user_id, activity_id),
    KEY idx_user_id (user_id),
    KEY idx_activity_id (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User coupon';

-- Message outbox table
CREATE TABLE IF NOT EXISTS message_outbox (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    biz_id VARCHAR(128) NOT NULL COMMENT 'Business unique ID (userId_activityId)',
    topic VARCHAR(128) NOT NULL COMMENT 'MQ topic',
    tag VARCHAR(64) COMMENT 'MQ tag',
    payload TEXT NOT NULL COMMENT 'Message payload (JSON)',
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'Status: PENDING, SENT, DEAD',
    retry_count INT NOT NULL DEFAULT 0 COMMENT 'Retry count',
    next_retry_time DATETIME COMMENT 'Next retry time',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_biz_id (biz_id),
    KEY idx_status_retry (status, retry_count, next_retry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Message outbox for reliable MQ delivery';

-- Alert record table
CREATE TABLE IF NOT EXISTS alert_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    alert_type VARCHAR(64) NOT NULL COMMENT 'Alert type',
    activity_id BIGINT COMMENT 'Activity ID',
    user_id BIGINT COMMENT 'User ID',
    message VARCHAR(512) NOT NULL COMMENT 'Alert message',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    PRIMARY KEY (id),
    KEY idx_alert_type (alert_type),
    KEY idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Alert records';
