package com.aidoc.aidocplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiTextDTO {
    @NotBlank(message = "待处理文本不能为空")
    @Size(max = 2000, message = "单次处理文本不能超过2000字")
    private String content;
    private String style; // 润色风格：专业/幽默/简洁，默认专业
}