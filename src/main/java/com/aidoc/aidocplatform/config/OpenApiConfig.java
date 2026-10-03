package com.aidoc.aidocplatform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI aiDocOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("AI 文档平台 API").version("1.0"))
                // 声明 JWT Bearer 认证方案，Knife4j 调试页会自动携带 Authorization 头
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .name("Authorization")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    /**
     * 给每个接口注入一个 Authorization 请求头参数，
     * 使其直接显示在 Knife4j「调试」页的「请求头部」表格中，可手动填写，最稳妥。
     */
    @Bean
    public OperationCustomizer authorizationHeaderCustomizer() {
        return (operation, handlerMethod) -> {
            Parameter authParam = new Parameter()
                    .in("header")
                    .name("Authorization")
                    .description("登录返回的Token，格式：Bearer <token>")
                    .required(false)
                    .schema(new StringSchema());
            if (operation.getParameters() == null) {
                operation.setParameters(new java.util.ArrayList<>());
            }
            operation.getParameters().add(authParam);
            return operation;
        };
    }
}
