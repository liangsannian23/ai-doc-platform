package com.aidoc.aidocplatform.service;

import java.util.Map;

/**
 * AI 能力服务接口：封装摘要、纠错、润色、文档问答四类大模型能力。
 * <p>底层通过 Spring AI 以 OpenAI 兼容协议接入讯飞星火；星火不可用时各方法具备降级策略。
 */
public interface IAiService {

    /**
     * 生成文本摘要。
     *
     * @param content 待摘要的原文（不超过 2000 字）
     * @return 不超过 200 字的摘要；AI 不可用时降级返回原文前 200 字
     */
    String summarize(String content);

    /**
     * 文本纠错，识别错别字与用词问题。
     *
     * @param content 待纠错的原文
     * @return 结果 Map，含 {@code errors}（错误列表）与 {@code correctedText}（纠正后文本）
     */
    Map<String, Object> proofread(String content);

    /**
     * 按指定风格润色文本。
     *
     * @param content 待润色的原文
     * @param style   润色风格：专业 / 幽默 / 简洁，为空时默认专业
     * @return 润色后的文本
     */
    String polish(String content, String style);

    /**
     * 基于指定文档内容进行问答（RAG）。
     *
     * @param documentId 目标文档 ID，须为当前用户有权访问的文档
     * @param userId     当前用户 ID，用于权限校验
     * @param question   用户提问
     * @return 基于文档的回答；与文档无关时返回“未找到相关信息”
     */
    String answerQuestion(Long documentId, Long userId, String question);
}