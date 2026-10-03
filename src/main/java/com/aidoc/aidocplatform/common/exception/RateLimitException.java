package com.aidoc.aidocplatform.common.exception;

/**
 * AI 接口限流异常（如星火 Lite 免费版 QPS 超限），由 GlobalExceptionHandler 统一转为 429 响应
 */
public class RateLimitException extends RuntimeException {

    public RateLimitException(String message) {
        super(message);
    }

    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
