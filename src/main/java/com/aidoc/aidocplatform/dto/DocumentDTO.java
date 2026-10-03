package com.aidoc.aidocplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DocumentDTO {

    @NotBlank(message = "文档标题不能为空")
    private String title;

    private String content;

    private Long categoryId;

    private Integer isPublic;
}