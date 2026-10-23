package com.aidoc.aidocplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * RAG 问答请求体：指定目标文档 + 问题。文档归属由当前登录用户校验。
 */
@Data
public class RagAskDTO {
    @NotNull(message = "请指定目标文档")
    private Long documentId;
    @NotBlank(message = "问题不能为空")
    private String question;
}
