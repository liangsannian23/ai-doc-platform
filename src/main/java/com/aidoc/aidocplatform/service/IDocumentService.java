package com.aidoc.aidocplatform.service;

import com.aidoc.aidocplatform.dto.DocumentQueryDTO;
import com.aidoc.aidocplatform.entity.Document;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IDocumentService extends IService<Document> {

    /**
     * 分页查询当前用户的文档列表
     */
    Page<Document> queryMyDocuments(Long userId, DocumentQueryDTO queryDTO);
}