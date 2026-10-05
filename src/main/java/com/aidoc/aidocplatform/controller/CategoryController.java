package com.aidoc.aidocplatform.controller;

import com.aidoc.aidocplatform.common.Result;
import com.aidoc.aidocplatform.common.utils.UserContext;
import com.aidoc.aidocplatform.entity.Category;
import com.aidoc.aidocplatform.service.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文档分类控制器：提供当前用户分类的增删改查接口。
 * <p>通过 {@code /api/**} 拦截器鉴权，用户身份由 {@link UserContext} 从 Token 解析。
 */
@Tag(name = "文档分类管理", description = "分类CRUD接口")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    /**
     * 新增分类。
     *
     * @param dto 含分类名称 name
     * @return 成功时 code=200
     */
    @Operation(summary = "新增分类")
    @PostMapping
    public Result<?> create(@RequestBody CategoryDTO dto) {
        Long userId = UserContext.getUserId();
        categoryService.createCategory(userId, dto.getName());
        return Result.success();
    }

    /**
     * 修改分类（仅能改本人分类）。
     *
     * @param id  目标分类 ID
     * @param dto 新分类名称
     * @return 成功时 code=200
     */
    @Operation(summary = "修改分类")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody CategoryDTO dto) {
        Long userId = UserContext.getUserId();
        categoryService.updateCategory(userId, id, dto.getName());
        return Result.success();
    }

    /**
     * 删除分类（仅能删本人分类）。
     *
     * @param id 目标分类 ID
     * @return 成功时 code=200
     */
    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        categoryService.deleteCategory(userId, id);
        return Result.success();
    }

    /**
     * 查询当前用户的全部分类。
     *
     * @return 分类列表
     */
    @Operation(summary = "查询我的分类列表")
    @GetMapping
    public Result<List<Category>> list() {
        Long userId = UserContext.getUserId();
        return Result.success(categoryService.listMyCategories(userId));
    }

    @Data
    public static class CategoryDTO {
        private String name;
    }
}