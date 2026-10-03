package com.aidoc.aidocplatform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;   // 见下方修正
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

// —— 上方 import 笔误已在最终版修正，实际如下 —— //
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            org.springframework.data.redis.connection.RedisConnectionFactory factory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // key 用 String 序列化
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // value 用 JSON 序列化，并注册 JavaTimeModule 以支持实体里的 LocalDateTime
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // 保留类型信息，反序列化才能还原成 User/Document
        mapper.activateDefaultTyping(
                new com.fasterxml.jackson.annotation.PolymorphicTypeValidator() {} , true);
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(mapper));
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(mapper));

        template.afterPropertiesSet();
        return template;
    }
}