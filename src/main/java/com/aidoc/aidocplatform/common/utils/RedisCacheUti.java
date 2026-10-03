package com.aidoc.aidocplatform.common.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class RedisCacheUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    /** Cache Aside 读：先查缓存，Miss 则执行 dbLoader 查库并回写 */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type, Supplier<T> dbLoader, long ttlSec) {
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.debug("[Cache HIT] key={}", key);
            return (T) cached;
        }
        T value = dbLoader.get();
        if (value != null) {
            redisTemplate.opsForValue().set(key, value, ttlSec, TimeUnit.SECONDS);
            log.debug("[Cache MISS->WRITE] key={}", key);
        }
        return value;
    }

    /** 主动失效（删除/更新后调用） */
    public void evict(String... keys) {
        for (String key : keys) {
            redisTemplate.delete(key);
            log.debug("[Cache EVICT] key={}", key);
        }
    }
}