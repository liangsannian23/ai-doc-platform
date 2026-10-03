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

@Tag(name = "文档管理", description = "文档CRUD接口")
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final IDocumentService documentService;

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

    @Operation(summary = "分页查询我的文档")
    @GetMapping("/page")
    public Result<Page<Document>> page(DocumentQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        Page<Document> page = documentService.queryMyDocuments(userId, queryDTO);
        return Result.success(page);
    }

    @Operation(summary = "查询文档详情")
    @GetMapping("/{id}")
    public Result<Document> detail(@PathVariable Long id) {
        Long currentUserId = UserContext.getUserId();

        Document doc = documentService.getById(id);
        if (doc == null) {
            return Result.error(404, "文档不存在或已被删除");
        }
        if (!doc.getOwnerId().equals(currentUserId)) {
            return Result.error(403, "无权查看他人文档");
        }
        return Result.success(doc);
    }
}