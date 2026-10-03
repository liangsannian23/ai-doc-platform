package com.aidoc.aidocplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiQaDTO {
    @NotNull(message = "请指定目标文档")
    private Long documentId;
    @NotBlank(message = "问题不能为空")
    private String question;
}