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
    /** RAG：仅依据检索到的相关片段作答，避免整篇硬塞 */
    public static final String RAG_QA =
            "请仅依据以下参考资料回答问题；若参考资料中无相关信息，只回答'未找到相关信息'。\n" +
                    "参考资料：\n%s\n问题：%s";
}