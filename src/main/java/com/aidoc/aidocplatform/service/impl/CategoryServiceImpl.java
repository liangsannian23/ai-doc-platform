package com.aidoc.aidocplatform.service.impl;

import com.aidoc.aidocplatform.entity.Category;
import com.aidoc.aidocplatform.entity.Document;
import com.aidoc.aidocplatform.mapper.CategoryMapper;
import com.aidoc.aidocplatform.service.ICategoryService;
import com.aidoc.aidocplatform.service.IDocumentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
        implements ICategoryService {

    private final IDocumentService documentService;

    @Override
    public void createCategory(Long userId, String name) {
        // 校验名称唯一性
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId)
                .eq(Category::getName, name);
        if (this.exists(wrapper)) {
            throw new RuntimeException("分类名称已存在");
        }

        Category category = new Category();
        category.setName(name);
        category.setUserId(userId);
        this.save(category);
    }

    @Override
    public void updateCategory(Long userId, Long categoryId, String name) {
        Category category = this.getById(categoryId);
        if (category == null || !category.getUserId().equals(userId)) {
            throw new RuntimeException("分类不存在或无权操作");
        }

        // 校验新名称唯一性（排除自己）
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId)
                .eq(Category::getName, name)
                .ne(Category::getId, categoryId);
        if (this.exists(wrapper)) {
            throw new RuntimeException("分类名称已存在");
        }

        category.setName(name);
        this.updateById(category);
    }

    @Override
    public void deleteCategory(Long userId, Long categoryId) {
        Category category = this.getById(categoryId);
        if (category == null || !category.getUserId().equals(userId)) {
            throw new RuntimeException("分类不存在或无权操作");
        }

        // 检查该分类下是否有关联文档
        LambdaQueryWrapper<Document> docWrapper = new LambdaQueryWrapper<>();
        docWrapper.eq(Document::getCategoryId, categoryId);
        if (documentService.count(docWrapper) > 0) {
            throw new RuntimeException("该分类下存在文档，无法删除");
        }

        this.removeById(categoryId);
    }

    @Override
    public List<Category> listMyCategories(Long userId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId);
        return this.list(wrapper);
    }
}