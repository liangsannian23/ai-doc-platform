package com.aidoc.aidocplatform.mq;

import com.aidoc.aidocplatform.dto.DocUpdateMessage;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DocumentNotifyConsumer {

    // ackMode 已在 application.yml 配为 manual，配合手动 basicAck/basicNack
    @RabbitListener(queues = "doc.notify.queue")
    public void handle(DocUpdateMessage msg, Channel channel,
                       @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("[收到文档通知] docId={}, userId={}, op={}",
                    msg.getDocId(), msg.getUserId(), msg.getOperation());
            // 模拟通知逻辑：发邮件 / 生成站内信
            sendNotification(msg);

            // 手动 ACK：成功才确认
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("[消费失败，进入死信]", e);
            try {
                // 拒绝且不重回队列 -> 触发死信交换机投递到 doc.dead.queue
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ignore) {
            }
        }
    }

    private void sendNotification(DocUpdateMessage msg) {
        log.info("模拟发送通知：用户{} 的文档{} 已被 {}",
                msg.getUserId(), msg.getDocId(), msg.getOperation());
    }
}
