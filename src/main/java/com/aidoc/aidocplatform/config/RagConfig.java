package com.aidoc.aidocplatform.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG 向量库配置。
 * <p>使用 {@link SimpleVectorStore}（内存实现）：零部署、重启即清空，够学习与演示。
 * Bean 类型声明为接口 {@link VectorStore}，将来换 pgvector/Redis 只改这一处，上层代码不动。
 * <p>{@link EmbeddingModel} 由 spring-ai-starter-model-openai 依据
 * {@code spring.ai.openai.embedding} 配置自动装配（指向讯飞 /v1/embeddings）。
 */
@Configuration
public class RagConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
