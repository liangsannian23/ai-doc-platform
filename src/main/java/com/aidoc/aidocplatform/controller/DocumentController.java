package com.aidoc.aidocplatform.controller;

import com.aidoc.aidocplatform.common.Result;
import com.aidoc.aidocplatform.common.utils.UserContext;
import com.aidoc.aidocplatform.dto.DocumentDTO;
import com.aidoc.aidocplatform.dto.DocumentQueryDTO;
import com.aidoc.aidocplatform.entity.Document;
import com.aidoc.aidocplatform.service.IDocumentService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 文档控制器：提供文档的创建、编辑、逻辑删除、分页查询与详情接口。
 * <p>写操作均校验文档归属（仅 owner 可操作）；详情接口走 Redis 缓存。
 */
@Tag(name = "文档管理", description = "文档CRUD接口")
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final IDocumentService documentService;

    /**
     * 创建文档，归属当前登录用户。
     *
     * @param dto 文档标题（必填）、内容、分类、是否公开
     * @return data 为新建文档的 ID
     */
    @Operation(summary = "创建文档")
    @PostMapping
    public Result<Long> create(@RequestBody @Valid DocumentDTO dto) {
        Long userId = UserContext.getUserId();

        Document doc = new Document();
        doc.setTitle(dto.getTitle());
        doc.setContent(dto.getContent());
        doc.setCategoryId(dto.getCategoryId());
        doc.setOwnerId(userId);
        doc.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : 0);

        documentService.save(doc);
        return Result.success(doc.getId());
    }

    /**
     * 编辑文档（仅 owner 可改）。
     *
     * @param id  文档 ID
     * @param dto 新的标题/内容/分类
     * @return 成功 code=200；不存在 404；非本人 403
     */
    @Operation(summary = "编辑文档")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody @Valid DocumentDTO dto) {
        Long currentUserId = UserContext.getUserId();

        // 1. 查询文档是否存在
        Document doc = documentService.getById(id);
        if (doc == null) {
            return Result.error(404, "文档不存在或已被删除");
        }

        // 2. 校验归属权
        if (!doc.getOwnerId().equals(currentUserId)) {
            return Result.error(403, "无权操作他人文档");
        }

        // 3. 更新字段
        doc.setTitle(dto.getTitle());
        doc.setContent(dto.getContent());
        doc.setCategoryId(dto.getCategoryId());
        documentService.updateById(doc);

        return Result.success();
    }

    /**
     * 删除文档（逻辑删除，仅 owner 可删）。
     *
     * @param id 文档 ID
     * @return 成功 code=200；不存在 404；非本人 403
     */
    @Operation(summary = "删除文档(逻辑删除)")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Long currentUserId = UserContext.getUserId();

        Document doc = documentService.getById(id);
        if (doc == null) {
            return Result.error(404, "文档不存在");
        }
        if (!doc.getOwnerId().equals(currentUserId)) {
            return Result.error(403, "无权操作他人文档");
        }

        documentService.removeById(id);  // MP逻辑删除，自动设置is_deleted=1
        return Result.success();
    }

    /**
     * 分页查询当前用户的文档列表。
     *
     * @param queryDTO 分页与查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询我的文档")
    @GetMapping("/page")
    public Result<Page<Document>> page(DocumentQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        Page<Document> page = documentService.queryMyDocuments(userId, queryDTO);
        return Result.success(page);
    }

    /**
     * 查询文档详情（走 Redis 缓存，仅 owner 可见）。
     *
     * @param id 文档 ID
     * @return 文档实体；不存在 404；非本人 403
     */
    @Operation(summary = "查询文档详情")
    @GetMapping("/{id}")
    public Result<Document> detail(@PathVariable Long id) {
        Long currentUserId = UserContext.getUserId();

        Document doc = documentService.getDocWithCache(id);
        if (doc == null) {
            return Result.error(404, "文档不存在或已被删除");
        }
        if (!doc.getOwnerId().equals(currentUserId)) {
            return Result.error(403, "无权查看他人文档");
        }
        return Result.success(doc);
    }
}