package com.aidoc.aidocplatform.common.exception;

/**
 * AI 服务调用失败异常（网络错误、鉴权失败、模型返回异常等），统一转为 503 响应
 */
public class AiServiceException extends RuntimeException {

    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
