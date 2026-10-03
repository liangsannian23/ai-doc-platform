package com.aidoc.aidocplatform.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {
    int value() default 30;        // 窗口内最大请求数
    int timeout() default 60;      // 窗口大小（秒）
    String message() default "操作过于频繁，请稍后再试";
}