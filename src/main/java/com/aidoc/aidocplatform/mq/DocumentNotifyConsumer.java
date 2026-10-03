package com.aidoc.aidocplatform.mq;

import com.aidoc.aidocplatform.dto.DocUpdateMessage;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@lombok.extern.slf4j.Slf4j
public class DocumentNotifyConsumer {

    // ackMode 需在 application.yml 配为 manual
    @RabbitListener(queues = "doc.notify.queue")
    public void handle(Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            DocUpdateMessage msg = (DocUpdateMessage) message.getBody(); // 经 Jackson 转换
            log.info("[收到文档通知] docId={}, userId={}, op={}",
                    msg.getDocId(), msg.getUserId(), msg.getOperation());
            // 模拟通知逻辑：发邮件 / 生成站内信
            sendNotification(msg);

            // 手动 ACK：成功才确认
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("[消费失败，进入重试/死信]", e);
            try {
                // 拒绝且不重回队列 -> 触发死信交换机投递到 doc.dead.queue
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ignore) {}
        }
    }

    private void sendNotification(DocUpdateMessage msg) {
        log.info("模拟发送通知：用户{} 的文档{} 已被 {}", msg.getUserId(),
                msg.getDocId(), msg.getOperation());
    }
}