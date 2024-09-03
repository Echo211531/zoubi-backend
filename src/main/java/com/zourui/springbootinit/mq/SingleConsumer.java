//package com.zourui.springbootinit.mq;
//
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.Connection;
//import com.rabbitmq.client.ConnectionFactory;
//import com.rabbitmq.client.DeliverCallback;
//
//import java.nio.charset.StandardCharsets;
//
////消费者
//public class SingleConsumer {
//
//    private final static String QUEUE_NAME = "hello";
//    public static void main(String[] argv) throws Exception {
//        // 建立链接
//        ConnectionFactory factory = new ConnectionFactory();
//        // 设置 rabbitmq 对应的信息
//        factory.setHost("localhost");
//        //factory.setUsername("xxx");
//        //factory.setPassword("xxx");
//
//        Connection connection = factory.newConnection();
//        Channel channel = connection.createChannel();
//        // 创建队列
//        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
//        // 定义如何处理消息
//        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
//            System.out.println(" [x] Received '" + message + "'");
//        };
//        // 消费消息
//        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
//    }
//}