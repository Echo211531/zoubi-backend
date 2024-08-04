package com.zourui.springbootinit.bimq;

import com.esotericsoftware.minlog.Log;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class BiDeadLetterMessageConsumer {

    @SneakyThrows
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_DEAD_NAME}, ackMode = "MANUAL")
    public void receiveDeadLetterMessage(String message, Channel channel,
                                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        Log.info("Received dead letter message: {}", message);
        
        // 处理死信消息
        // ...

        // 确认消息
        channel.basicAck(deliveryTag, false);
    }
}