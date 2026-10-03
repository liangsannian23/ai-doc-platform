-- 滑动窗口限流（ZSET 实现）
-- KEYS[1] : 限流 key（rate:{userId}:{接口签名}）
-- ARGV[1] : 当前时间戳 now（毫秒）
-- ARGV[2] : 窗口大小 window（毫秒）
-- ARGV[3] : 窗口内允许的最大请求数 limit
-- ARGV[4] : 本次请求的唯一成员值 member
-- 返回 1 = 放行，0 = 拒绝
local key    = KEYS[1]
local now    = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local limit  = tonumber(ARGV[3])
local member = ARGV[4]

-- 1. 移除窗口外的过期记录（score 即请求时间戳）
redis.call('ZREMRANGEBYSCORE', key, 0, now - window)

-- 2. 统计当前窗口内的请求数
local count = redis.call('ZCARD', key)

if count < limit then
    -- 3. 未超限：记录本次请求，并设置 key 过期（窗口大小，避免残留）
    redis.call('ZADD', key, now, member)
    redis.call('PEXPIRE', key, window)
    return 1
else
    return 0
end
