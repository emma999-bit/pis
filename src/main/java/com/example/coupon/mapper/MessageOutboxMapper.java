package com.example.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.coupon.model.entity.MessageOutbox;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageOutboxMapper extends BaseMapper<MessageOutbox> {

    /**
     * Query PENDING outbox messages eligible for retry:
     * status=PENDING, retry_count < 5, next_retry_time <= now (or null).
     *
     * @param maxRetryCount maximum retry count threshold
     * @return list of eligible pending outbox messages
     */
    List<MessageOutbox> findPendingForRetry(@Param("maxRetryCount") int maxRetryCount);
}
