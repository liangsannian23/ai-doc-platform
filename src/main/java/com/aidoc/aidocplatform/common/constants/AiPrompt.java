package com.aidoc.aidocplatform.common.constants;

public class AiPrompt {
    public static final String SUMMARY =
            "请对以下文本进行200字以内的中文摘要，只输出摘要正文：\n%s";
    public static final String PROOFREAD =
            "请找出以下文本的错别字和语病。严格按JSON输出，格式为 " +
                    "{\"errors\":[{\"wrong\":\"\",\"right\":\"\",\"reason\":\"\"}],\"correctedText\":\"\"}：" +
                    "\n%s";
    public static final String POLISH =
            "请将以下文本润色为【%s】风格，使其专业流畅，只输出润色后文本：\n%s";
    public static final String QA =
            "请仅基于以下文档内容回答问题，若文档中无相关信息只回答'未找到相关信息'。" +
                    "文档：%s\n问题：%s";
}