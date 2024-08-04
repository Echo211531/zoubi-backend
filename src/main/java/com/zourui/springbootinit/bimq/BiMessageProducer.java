package com.zourui.springbootinit.bimq;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Component
public class BiMessageProducer {
    @Resource
    private RabbitTemplate rabbitMqTemplate;

    /**
     * 发送消息
     * @param message
     */
    public void sendMessage(String message) {
        rabbitMqTemplate.convertAndSend(BiMqConstant.BI_EXCHANGE_NAME, BiMqConstant.BI_ROUTING_KEY, message);
    }
//    /**
//     * 发送消息，异步进行redis和mysql双写一致处理开启
//     * @param message
//     */
//    public void sendMysqlAndRedis(String message) {
//        rabbitMqTemplate.convertAndSend(BiMqConstant.BI_EXCHANGE_NAME, BiMqConstant.BI_REDISSQL_KEY, message);
//    }
}
