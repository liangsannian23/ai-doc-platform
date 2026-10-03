package com.aidoc.aidocplatform.service;

import java.util.Map;

public interface IAiService {
    String summarize(String content);
    Map<String, Object> proofread(String content);
    String polish(String content, String style);
    String answerQuestion(Long documentId, Long userId, String question);
}