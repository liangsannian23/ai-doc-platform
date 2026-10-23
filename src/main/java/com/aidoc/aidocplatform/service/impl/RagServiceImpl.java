package com.aidoc.aidocplatform.service.impl;

import com.aidoc.aidocplatform.common.constants.AiPrompt;
import com.aidoc.aidocplatform.common.exception.AiServiceException;
import com.aidoc.aidocplatform.entity.Document;               // 业务实体（与 Spring AI 的 Document 同名，故下面用全限定名区分）
import com.aidoc.aidocplatform.service.IDocumentService;
import com.aidoc.aidocplatform.service.IRagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG 服务实现：入库走“切块→向量化→存储”，问答走“检索 Top-K→拼上下文→生成”。
 * <p>向量库用内存版 {@link VectorStore}；每次检索多取一些再按 docId 过滤，保证只命中本文档、绝不串到他人文档。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagServiceImpl implements IRagService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final IDocumentService documentService;

    /** 最终喂给模型的片段数 */
    private static final int TOP_K = 3;
    /** 先多召回一些，再按 docId 过滤，避免被别的文档挤掉 */
    private static final int RECALL_K = TOP_K * 5;

    private static final String META_DOC_ID = "docId";
    private static final String META_OWNER_ID = "ownerId";

    @Override
    public int indexDocument(Long documentId, Long userId) {
        Document doc = requireOwnedDoc(documentId, userId);

        // 1) 整篇作为一个 Spring AI Document，挂上 docId/ownerId 元数据（值统一转字符串，便于过滤比对）
        Map<String, Object> meta = new HashMap<>();
        meta.put(META_DOC_ID, String.valueOf(documentId));
        meta.put(META_OWNER_ID, String.valueOf(userId));
        org.springframework.ai.document.Document whole =
                new org.springframework.ai.document.Document(doc.getContent(), meta);

        // 2) 切块：TokenTextSplitter 会把父文档的 metadata 复制到每个子块，故每块都带 docId
        List<org.springframework.ai.document.Document> chunks =
                new TokenTextSplitter().apply(List.of(whole));
        if (chunks.isEmpty()) {
            throw new RuntimeException("文档切块结果为空，无法入库");
        }

        // 3) 入库：VectorStore.add 内部会自动调用 EmbeddingModel 把每块文本向量化
        vectorStore.add(chunks);
        log.debug("[RAG] 文档 {} 入库 {} 个切片", documentId, chunks.size());
        return chunks.size();
    }

    @Override
    public String ask(Long documentId, Long userId, String question) {
        requireOwnedDoc(documentId, userId);

        // 1) 检索：问题向量化后做相似度搜索，多召回一些
        List<org.springframework.ai.document.Document> hits = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(RECALL_K).build());

        // 2) 过滤：只保留属于本文档的片段，取前 TOP_K
        String docIdStr = String.valueOf(documentId);
        List<org.springframework.ai.document.Document> relevant = hits.stream()
                .filter(d -> docIdStr.equals(String.valueOf(d.getMetadata().get(META_DOC_ID))))
                .limit(TOP_K)
                .collect(Collectors.toList());

        if (relevant.isEmpty()) {
            return "未找到相关信息（该文档可能尚未建立索引，或问题与文档内容无关）";
        }

        // 3) 增强生成：把检索到的片段拼成上下文，喂给星火作答
        String context = relevant.stream()
                .map(org.springframework.ai.document.Document::getText)
                .collect(Collectors.joining("\n---\n"));
        String prompt = String.format(AiPrompt.RAG_QA, context, question);
        try {
            return chatClient.prompt().user(prompt).call().content();
        } catch (Exception e) {
            log.error("RAG 生成失败", e);
            throw new AiServiceException("AI服务繁忙，请稍后重试", e);
        }
    }

    /** 读取文档并校验归属与内容，复用与 AiServiceImpl 一致的规则 */
    private Document requireOwnedDoc(Long documentId, Long userId) {
        Document doc = documentService.getById(documentId);
        if (doc == null) throw new RuntimeException("文档不存在或已被删除");
        if (!doc.getOwnerId().equals(userId)) throw new RuntimeException("无权操作他人文档");
        if (!StringUtils.hasText(doc.getContent())) throw new RuntimeException("文档内容为空，无法处理");
        return doc;
    }
}
