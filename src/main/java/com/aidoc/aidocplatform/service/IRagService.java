package com.aidoc.aidocplatform.service;

/**
 * RAG（检索增强生成）服务：把文档切块向量化入库，问答时先检索相关片段再喂给大模型作答。
 * <p>与 {@link IAiService#answerQuestion} 的“整篇塞 prompt”不同，这里是真正的“先检索、再生成”。
 */
public interface IRagService {

    /**
     * 文档入库：读取文档 → 切块 → 向量化 → 存入向量库（带 docId/ownerId 元数据）。
     *
     * @param documentId 目标文档 ID，须为当前用户拥有的文档
     * @param userId     当前用户 ID，用于权限校验
     * @return 成功入库的切片数量
     */
    int indexDocument(Long documentId, Long userId);

    /**
     * RAG 问答：问题向量化 → 检索该文档的 Top-K 相关片段 → 拼上下文 → 大模型作答。
     *
     * @param documentId 目标文档 ID
     * @param userId     当前用户 ID
     * @param question   用户提问
     * @return 基于检索片段的回答；无相关片段时返回提示语
     */
    String ask(Long documentId, Long userId, String question);
}
