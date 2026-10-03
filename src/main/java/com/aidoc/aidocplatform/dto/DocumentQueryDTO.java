package com.aidoc.aidocplatform.dto;

import lombok.Data;

@Data
public class DocumentQueryDTO {

    private Integer current = 1;   // 当前页
    private Integer size = 10;     // 每页大小
    private Long categoryId;       // 分类ID（可选）
    private String keyword;        // 关键词模糊搜索（可选）
}