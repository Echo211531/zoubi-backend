package com.zourui.springbootinit.bimq;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class QueueInitializer implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        initializeQueues();
    }

    private void initializeQueues() {   //创建一个监听器来监听,每次启动项目都会执行一次消息队列初始化
        try {
            ConnectionFactory factory = new ConnectionFactory();
            //factory.setHost("localhost");
            factory.setHost("110.40.137.152");
            factory.setPassword("Zr13970309103"); // 设置密码
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();          //获取频道

            //定义队列 交换机 路由键
            String queueName = BiMqConstant.BI_QUEUE_NAME;
            String exchangeName = BiMqConstant.BI_EXCHANGE_NAME;
            String key= BiMqConstant.BI_ROUTING_KEY;
            String deadQueueName=BiMqConstant.BI_QUEUE_DEAD_NAME;
            String deadExchangeName = BiMqConstant.BI_EXCHANGE_DEAD;
            String deadKey= BiMqConstant.BI_ROUTING_DEAD_KEY;
            // 创建一个队列，用做实现mysql和redis双写一致，分配一个队列名称
            //String queueRedisAndMysqlName = BiMqConstant.BI_Redis_Mysql;

            channel.exchangeDeclare(exchangeName, "direct");     // 创建工作交换机
            channel.exchangeDeclare(deadExchangeName, "direct");    //创建死信交换机

            // 通过设置 x-message-ttl 参数来指定消息的过期时间
            Map<String, Object> queueArgs = new HashMap<>();
            //queueArgs.put("x-message-ttl", 6000000); // 过期时间为 6000 秒
            //创建工作队列，持久，不排他，不自动删除，没有过期时间
            channel.queueDeclare(queueName, true, false, false, queueArgs);
            //创建双写一致队列
            // channel.queueDeclare(queueRedisAndMysqlName,true,false,false,null);
            //为其设置死信交换机
            Map<String, Object> deadArgs = new HashMap<>();
            deadArgs.put("x-dead-letter-exchange",deadExchangeName );
            deadArgs.put("x-dead-letter-routing-key",deadKey);
            // 将两个队列与交换机进行绑定
            channel.queueBind(queueName,exchangeName, key,deadArgs);
            //channel.queueBind(queueRedisAndMysqlName, exchangeName, BiMqConstant.BI_REDISSQL_KEY, deadArgs);

            // 声明死信队列，并将其绑定到死信交换机
            channel.queueDeclare(deadQueueName,true,false,false,null);
            channel.queueBind(deadQueueName,deadExchangeName,deadKey);

        } catch (Exception e) {
            // 处理异常情况
        }
    }
}