package com.aidoc.aidocplatform.common.aspect;

import com.aidoc.aidocplatform.common.annotation.RateLimiter;
import com.aidoc.aidocplatform.common.exception.RateLimitException;
import com.aidoc.aidocplatform.common.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

@Aspect
@Component
@Slf4j
public class RateLimiterAspect {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> rateScript;

    // 手动构造器统一注入两个字段（不再用 @RequiredArgsConstructor，避免生成重复构造器导致注入歧义）
    public RateLimiterAspect(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.rateScript = RedisScript.of(new ClassPathResource("lua/rate_limit.lua"), Long.class);
    }

    @Around("@annotation(rateLimiter)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) throws Throwable {
        Long userId = UserContext.getUserId();
        String key = "rate:" + userId + ":" +
                joinPoint.getSignature().toShortString();

        long now = System.currentTimeMillis();
        long windowMs = rateLimiter.timeout() * 1000L;
        String member = now + "-" + UUID.randomUUID();

        try {
            Long allow = redisTemplate.execute(rateScript,
                    Collections.singletonList(key),
                    String.valueOf(now), String.valueOf(windowMs),
                    String.valueOf(rateLimiter.value()), member);
            if (allow == null || allow == 0L) {
                throw new RateLimitException(rateLimiter.message());
            }
        } catch (RateLimitException e) {
            throw e;                       // 命中限流，正常向上抛（→ 429）
        } catch (Exception e) {
            // Redis 故障降级：放行，不因限流组件拖垮 AI 主流程
            log.warn("限流 Redis 异常，降级放行：{}", e.getMessage());
        }
        return joinPoint.proceed();
    }
}