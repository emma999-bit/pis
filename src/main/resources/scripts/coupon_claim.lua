-- KEYS[1] = coupon:stock:{activityId}
-- KEYS[2] = coupon:claimed:{activityId}
-- ARGV[1] = userId

-- 1. 用户去重
local claimed = redis.call('SISMEMBER', KEYS[2], ARGV[1])
if claimed == 1 then
    return -2
end

-- 2. 库存检查
local stock = redis.call('GET', KEYS[1])
if stock == false then
    return -1
end

if tonumber(stock) <= 0 then
    return 0
end

-- 3. 原子扣减
redis.call('DECR', KEYS[1])
redis.call('SADD', KEYS[2], ARGV[1])
return 1
