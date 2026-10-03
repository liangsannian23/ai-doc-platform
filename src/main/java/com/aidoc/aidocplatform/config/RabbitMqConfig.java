package com.aidoc.aidocplatform.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE   = "doc.exchange";
    public static final String QUEUE      = "doc.notify.queue";
    public static final String ROUTING_KEY= "doc.update";

    // 业务交换机（Topic）
    @Bean
    public TopicExchange docExchange() { return new TopicExchange(EXCHANGE, true, false); }

    // 业务队列（绑定死信）
    @Bean
    public Queue docNotifyQueue() {
        return QueueBuilder.durable(QUEUE)
                .deadLetterExchange("doc.dlx")           // 死信交换机
                .deadLetterRoutingKey("doc.dead")
                .build();
    }

    @Bean
    public Binding docBinding() {
        return BindingBuilder.bind(QUEUE).to(docExchange()).with(ROUTING_KEY);
    }

    // ===== 死信结构 =====
    @Bean public DirectExchange docDlx() { return new DirectExchange("doc.dlx", true, false); }
    @Bean public Queue docDeadQueue() { return QueueBuilder.durable("doc.dead.queue").build(); }
    @Bean public Binding docDeadBinding() {
        return BindingBuilder.bind("doc.dead.queue").to(docDlx()).with("doc.dead");
    }

    // JSON 消息转换器（生产/消费统一用 JSON）
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            org.springframework.amqp.rabbit.connection.ConnectionFactory cf,
            Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(converter);
        return template;
    }
}