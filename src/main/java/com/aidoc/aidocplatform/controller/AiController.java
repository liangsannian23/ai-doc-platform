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

/**
 * AI 能力控制器：对外提供文档摘要、文本纠错、内容润色、文档问答四个接口。
 * <p>所有接口均需登录（携带 {@code Authorization: Bearer <token>}），
 * 并通过 {@link RateLimiter} 做滑动窗口限流（每用户每接口 60 秒 5 次）。
 */
@Tag(name = "AI能力", description = "摘要/纠错/润色/问答")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final IAiService aiService;
    // 【删除】private final AiRateLimiter rateLimiter;

    /**
     * 文档摘要。
     *
     * @param dto 含待处理文本 content（非空且≤2000字）
     * @return 不超过 200 字的摘要
     */
    @Operation(summary = "文档摘要")
    @RateLimiter(value = 5, timeout = 60) // 验收：1分钟>5次→429；生产改 value=30
    @PostMapping("/summary")
    public Result<String> summary(@RequestBody @Valid AiTextDTO dto) {
        // 【删除】rateLimiter.tryAcquire(UserContext.getUserId());
        return Result.success(aiService.summarize(dto.getContent()));
    }

    /**
     * 文本纠错。
     *
     * @param dto 含待纠错文本 content
     * @return 含 errors 列表与 correctedText 纠正后文本
     */
    @Operation(summary = "文本纠错")
    @RateLimiter(value = 5, timeout = 60)
    @PostMapping("/proofread")
    public Result<Map<String, Object>> proofread(@RequestBody @Valid AiTextDTO dto) {
        // 【删除】rateLimiter.tryAcquire(UserContext.getUserId());
        return Result.success(aiService.proofread(dto.getContent()));
    }

    /**
     * 内容润色。
     *
     * @param dto 含 content 与可选 style（专业/幽默/简洁）
     * @return 润色后的文本
     */
    @Operation(summary = "内容润色")
    @RateLimiter(value = 5, timeout = 60)
    @PostMapping("/polish")
    public Result<String> polish(@RequestBody @Valid AiTextDTO dto) {
        // 【删除】rateLimiter.tryAcquire(UserContext.getUserId());
        return Result.success(aiService.polish(dto.getContent(), dto.getStyle()));
    }

    /**
     * 文档问答（RAG）。
     *
     * @param dto 含 documentId 与 question；文档归属由当前用户校验
     * @return 基于文档的回答，无关问题返回“未找到相关信息”
     */
    @Operation(summary = "文档问答")
    @RateLimiter(value = 5, timeout = 60)
    @PostMapping("/qa")
    public Result<String> qa(@RequestBody @Valid AiQaDTO dto) {
        // 【删除】rateLimiter.tryAcquire(UserContext.getUserId());
        return Result.success(
                aiService.answerQuestion(dto.getDocumentId(), UserContext.getUserId(), dto.getQuestion()));
    }
}