package com.aidoc.aidocplatform.common;


import com.aidoc.aidocplatform.common.exception.AiServiceException;
import com.aidoc.aidocplatform.common.exception.RateLimitException;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;

import java.util.stream.Collectors;

@RestControllerAdvice
@Hidden  // 规避 springdoc 2.3.0 与 spring-web 6.2 的 ControllerAdviceBean 兼容问题，不纳入文档扫描
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.error(400, message);
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        // 打印完整堆栈，便于定位真实异常（getMessage 可能为 null）
        log.error("运行时异常", e);
        String msg = e.getMessage();
        if (msg == null || msg.isEmpty()) {
            msg = e.getClass().getSimpleName();  // 例如 NullPointerException
        }
        // 顺着异常链找到根因（如 MyBatisSystemException 本身无 message，真实原因在 cause 里）
        Throwable root = e;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        if (root != e) {
            msg = msg + " | 根因: " + root.getClass().getSimpleName() + ": " + root.getMessage();
        }
        return Result.error(500, msg);
    }

    @ExceptionHandler(RateLimitException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)   // 429
    public Result<?> handleRateLimit(RateLimitException e) {
        return Result.error(429, e.getMessage());
    }

    @ExceptionHandler(AiServiceException.class)    // 可选：单独定义AI业务异常
    public Result<?> handleAiService(AiServiceException e) {
        return Result.error(503, "AI服务暂不可用：" + e.getMessage());
    }
}