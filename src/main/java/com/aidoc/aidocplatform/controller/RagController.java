package com.aidoc.aidocplatform.controller;

import com.aidoc.aidocplatform.common.Result;
import com.aidoc.aidocplatform.common.annotation.RateLimiter;
import com.aidoc.aidocplatform.common.utils.UserContext;
import com.aidoc.aidocplatform.dto.RagAskDTO;
import com.aidoc.aidocplatform.service.IRagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * RAG 知识库问答控制器。
 * <p>需登录（{@code /api/**} 已被登录拦截器覆盖）；问答接口做滑动窗口限流。
 * <p>典型用法：先 {@code POST /api/rag/index/{documentId}} 建索引，再 {@code POST /api/rag/ask} 提问。
 */
@Tag(name = "RAG知识库问答", description = "检索增强生成")
@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final IRagService ragService;

    /**
     * 文档入库（建立向量索引）。
     *
     * @param documentId 目标文档 ID（须为当前用户拥有）
     * @return 成功入库的切片数
     */
    @Operation(summary = "文档入库/建索引")
    @PostMapping("/index/{documentId}")
    public Result<Integer> index(@PathVariable Long documentId) {
        return Result.success(ragService.indexDocument(documentId, UserContext.getUserId()));
    }

    /**
     * RAG 问答：检索该文档相关片段后由大模型作答。
     *
     * @param dto 含 documentId 与 question
     * @return 基于检索片段的回答
     */
    @Operation(summary = "RAG 问答")
    @RateLimiter(value = 5, timeout = 60)
    @PostMapping("/ask")
    public Result<String> ask(@RequestBody @Valid RagAskDTO dto) {
        return Result.success(
                ragService.ask(dto.getDocumentId(), UserContext.getUserId(), dto.getQuestion()));
    }
}
