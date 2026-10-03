package com.aidoc.aidocplatform.service;

import com.aidoc.aidocplatform.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface ICategoryService extends IService<Category> {
    void createCategory(Long userId, String name);
    void updateCategory(Long userId, Long categoryId, String name);
    void deleteCategory(Long userId, Long categoryId);
    List<Category> listMyCategories(Long userId);
}