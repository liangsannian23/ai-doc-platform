package com.aidoc.aidocplatform.controller;

import com.aidoc.aidocplatform.common.Result;
// 【新增】导入自定义限流注解，请根据实际包路径修改
import com.aidoc.aidocplatform.common.annotation.RateLimiter;
import com.aidoc.aidocplatform.common.utils.UserContext;
import com.aidoc.aidocplatform.dto.AiQaDTO;
import com.aidoc.aidocplatform.dto.AiTextDTO;
import com.aidoc.aidocplatform.service.IAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "AI能力", description = "摘要/纠错/润色/问答")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final IAiService aiService;
    // 【删除】private final AiRateLimiter rateLimiter;

    @Operation(summary = "文档摘要")
    @RateLimiter(value = 5, timeout = 60) // 验收：1分钟>5次→429；生产改 value=30
    @PostMapping("/summary")
    public Result<String> summary(@RequestBody @Valid AiTextDTO dto) {
        // 【删除】rateLimiter.tryAcquire(UserContext.getUserId());
        return Result.success(aiService.summarize(dto.getContent()));
    }

    @Operation(summary = "文本纠错")
    @RateLimiter(value = 5, timeout = 60)
    @PostMapping("/proofread")
    public Result<Map<String, Object>> proofread(@RequestBody @Valid AiTextDTO dto) {
        // 【删除】rateLimiter.tryAcquire(UserContext.getUserId());
        return Result.success(aiService.proofread(dto.getContent()));
    }

    @Operation(summary = "内容润色")
    @RateLimiter(value = 5, timeout = 60)
    @PostMapping("/polish")
    public Result<String> polish(@RequestBody @Valid AiTextDTO dto) {
        // 【删除】rateLimiter.tryAcquire(UserContext.getUserId());
        return Result.success(aiService.polish(dto.getContent(), dto.getStyle()));
    }

    @Operation(summary = "文档问答")
    @RateLimiter(value = 5, timeout = 60)
    @PostMapping("/qa")
    public Result<String> qa(@RequestBody @Valid AiQaDTO dto) {
        // 【删除】rateLimiter.tryAcquire(UserContext.getUserId());
        return Result.success(
                aiService.answerQuestion(dto.getDocumentId(), UserContext.getUserId(), dto.getQuestion()));
    }
}