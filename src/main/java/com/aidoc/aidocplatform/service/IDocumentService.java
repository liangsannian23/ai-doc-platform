package com.aidoc.aidocplatform.service;

import com.aidoc.aidocplatform.dto.DocumentQueryDTO;
import com.aidoc.aidocplatform.entity.Document;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 文档服务接口：提供文档分页查询与带缓存的详情读取。
 * <p>继承 MyBatis-Plus {@link IService} 以获得基础 CRUD 与逻辑删除能力。
 */
public interface IDocumentService extends IService<Document> {

    /**
     * 分页查询当前用户的文档列表
     */
    Page<Document> queryMyDocuments(Long userId, DocumentQueryDTO queryDTO);

    /**
     * 带 Redis 缓存的文档详情（Cache Aside + 1小时TTL）
     */
    Document getDocWithCache(Long docId);
}
