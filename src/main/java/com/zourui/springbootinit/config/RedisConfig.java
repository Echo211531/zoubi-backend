package com.zourui.springbootinit.config;

//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
//
//@Configuration
//public class RedisConfig {
//    /**
//     * 自定义 RedisTemplate
//     * 修改 Redis 序列化方式，默认 JdkSerializationRedisSerializer
//     */
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
//
//        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//
//        // 使用Jackson2JsonRedisSerializer作为序列化器,提高序列化效率
//        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule()); // 注册Java 8日期/时间模块
//        serializer.setObjectMapper(objectMapper);
//
//        redisTemplate.setDefaultSerializer(serializer);
//        redisTemplate.setValueSerializer(serializer);
//        redisTemplate.setHashValueSerializer(serializer);
//        return redisTemplate;
//    }
//}