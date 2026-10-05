package com.aidoc.aidocplatform.service;

import com.aidoc.aidocplatform.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 文档分类服务接口：管理当前用户自己的分类。
 * <p>继承 MyBatis-Plus {@link IService} 以获得基础 CRUD 能力。
 */
public interface ICategoryService extends IService<Category> {

    /**
     * 新增分类。
     *
     * @param userId 归属用户 ID
     * @param name   分类名称
     */
    void createCategory(Long userId, String name);

    /**
     * 修改分类名称，仅允许操作本人分类。
     *
     * @param userId     当前用户 ID（用于权限校验）
     * @param categoryId 目标分类 ID
     * @param name       新名称
     */
    void updateCategory(Long userId, Long categoryId, String name);

    /**
     * 删除分类，仅允许操作本人分类。
     *
     * @param userId     当前用户 ID（用于权限校验）
     * @param categoryId 目标分类 ID
     */
    void deleteCategory(Long userId, Long categoryId);

    /**
     * 查询当前用户的全部分类。
     *
     * @param userId 用户 ID
     * @return 该用户的分类列表
     */
    List<Category> listMyCategories(Long userId);
}