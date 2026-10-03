package com.aidoc.aidocplatform.common.utils;

import com.aidoc.aidocplatform.common.exception.RateLimitException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 【阶段4 旧实现 · 仅作复盘参考，已由 RateLimiterAspect 取代】
 * AI 接口限流器（固定窗口计数，每用户每分钟最多 30 次）
 * <p>
 * 优先使用 Redis INCR 做分布式计数；当 Redis 不可用时（如本地开发尚未启动 Redis），
 * 自动降级为进程内 {@link ConcurrentHashMap} 计数，保证限流语义不丢失。
 * 超限抛 {@link RateLimitException}，由全局异常处理器统一转 429。
 * <p>
 * 演进对比（阶段5 改用 {@code RateLimiterAspect} + Redis Lua 滑动窗口）：
 * <ul>
 *   <li>触发方式：本类手写 tryAcquire 直调 → 切面 {@code @RateLimiter} 注解，无侵入</li>
 *   <li>算法：本类固定窗口（按分钟边界，存在临界突刺）→ 滑动窗口（ZSET，无突刺）</li>
 *   <li>降级：本类 Redis 故障转内存计数 → 切面 Redis 故障直接放行（fail-open）</li>
 *   <li>原子性：本类单命令 INCR 天然原子 → 切面靠 Lua 保证“查+写”原子</li>
 * </ul>
 * 保留本类用于对比学习；已去掉 {@code @Component}，不再作为 Bean 实例化，生产不生效。
 */
@Deprecated
@Slf4j
@RequiredArgsConstructor
public class AiRateLimiter {

    /** 每用户每分钟允许的 AI 调用次数 */
    private static final int LIMIT_PER_MINUTE = 30;

    /** Redis key 前缀，key 形如 ai:rate:{userId}:{分钟窗口号} */
    private static final String KEY_PREFIX = "ai:rate:";

    private final StringRedisTemplate redisTemplate;

    /** 内存降级计数表：key = {userId}:{分钟窗口号}，value = 当前窗口调用次数 */
    private final Map<String, AtomicInteger> localCounters = new ConcurrentHashMap<>();

    /**
     * 尝试获取一次调用配额，超限直接抛异常
     */
    public void tryAcquire(Long userId) {
        if (userId == null) {
            return;  // 无法识别用户时不限流（fail-open），避免误伤
        }

        long minuteWindow = System.currentTimeMillis() / 60_000;
        int count = acquireCount(userId, minuteWindow);

        if (count > LIMIT_PER_MINUTE) {
            log.info("用户 {} 触发 AI 限流：{} 次/分钟（上限 {}）", userId, count, LIMIT_PER_MINUTE);
            throw new RateLimitException("AI调用过于频繁，请1分钟后再试");
        }
    }

    /**
     * 返回本次调用后该用户在当前窗口的累计次数。
     * Redis 正常时走分布式 INCR；Redis 故障（连接失败等）时降级为本地内存计数。
     */
    private int acquireCount(Long userId, long minuteWindow) {
        try {
            String key = KEY_PREFIX + userId + ":" + minuteWindow;
            Long count = redisTemplate.opsForValue().increment(key);
            if (count == null) {
                throw new IllegalStateException("Redis INCR 返回 null");
            }
            if (count == 1L) {
                // 窗口首次进入时设置过期；TTL 给 2 分钟冗余，即使 expire 偶发失败，
                // 下一窗口用的是新 key，旧 key 也会自然被兜底清理，不会永久残留
                redisTemplate.expire(key, 120, TimeUnit.SECONDS);
            }
            return count.intValue();
        } catch (Exception e) {
            // Redis 不可用：降级到进程内计数，保证限流功能本身不因中间件故障而失效
            log.warn("Redis 限流计数失败，降级为内存计数：{}", e.getMessage());
            return incrementLocal(userId, minuteWindow);
        }
    }

    /**
     * 内存固定窗口计数：窗口号变化时自然重置（旧窗口 key 顺带清理，避免无限增长）
     */
    private int incrementLocal(Long userId, long minuteWindow) {
        // 清理非当前窗口的残留 key（单机场景数量很少，直接遍历即可）
        localCounters.keySet().removeIf(k -> !k.endsWith(":" + minuteWindow));
        String key = userId + ":" + minuteWindow;
        return localCounters.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
    }
}
