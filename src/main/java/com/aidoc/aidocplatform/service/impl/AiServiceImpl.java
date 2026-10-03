package com.aidoc.aidocplatform.service.impl;

import com.aidoc.aidocplatform.common.constants.AiPrompt;
import com.aidoc.aidocplatform.common.exception.AiServiceException;
import com.aidoc.aidocplatform.entity.Document;
import com.aidoc.aidocplatform.service.IAiService;
import com.aidoc.aidocplatform.service.IDocumentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements IAiService {

    private final ChatClient chatClient;
    private final IDocumentService documentService;
    private final ObjectMapper objectMapper;

    private static final int MAX_LEN = 2000;

    /** 截断到2000字 */
    private String truncate(String text) {
        return text.length() > MAX_LEN ? text.substring(0, MAX_LEN) : text;
    }

    /** 调用 ChatClient，统一捕获底层异常 → 友好提示 */
    private String call(String prompt) {
        try {
            return chatClient.prompt().user(prompt).call().content();
        } catch (Exception e) {
            log.error("AI 调用失败", e);
            throw new AiServiceException("AI服务繁忙，请稍后重试", e);
        }
    }

    @Override
    public String summarize(String content) {
        if (!StringUtils.hasText(content)) throw new RuntimeException("文本不能为空");
        String text = truncate(content);
        try {
            return call(String.format(AiPrompt.SUMMARY, text));
        } catch (Exception e) {
            // 降级：返回原文前200字
            return text.length() > 200 ? text.substring(0, 200) + "..." : text;
        }
    }

    @Override
    public Map<String, Object> proofread(String content) {
        if (!StringUtils.hasText(content)) throw new RuntimeException("文本不能为空");
        String raw = call(String.format(AiPrompt.PROOFREAD, truncate(content)));
        try {
            String json = raw.replaceAll("(?s).*?(\\{.*\\}).*?", "$1"); // 抽取JSON
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            // 解析失败时兜底返回原文
            return Map.of("errors", java.util.List.of(), "correctedText", content);
        }
    }

    @Override
    public String polish(String content, String style) {
        if (!StringUtils.hasText(content)) throw new RuntimeException("文本不能为空");
        String s = StringUtils.hasText(style) ? style : "专业";
        return call(String.format(AiPrompt.POLISH, s, truncate(content)));
    }

    @Override
    public String answerQuestion(Long documentId, Long userId, String question) {
        Document doc = documentService.getById(documentId);
        if (doc == null) throw new RuntimeException("文档不存在或已被删除");
        if (!doc.getOwnerId().equals(userId)) throw new RuntimeException("无权操作他人文档");
        if (!StringUtils.hasText(doc.getContent())) throw new RuntimeException("文档内容为空，无法问答");
        return call(String.format(AiPrompt.QA, truncate(doc.getContent()), question));
    }
}