package com.zourui.springbootinit.config;
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Data
//@ConfigurationProperties(prefix = "spring.redis")
//@Configuration
public class RedissonConfig {
//    private Integer database;
//    private String host;
//    private Integer port;
//    private String password;
//    @Bean
//    public RedissonClient redissonClient() {
//        Config config = new Config();
//        config.useSingleServer()
//                .setDatabase(database)
//                .setAddress("redis://" + host + ":" + port)
//                .setPassword(password);
//        RedissonClient redissonClient = Redisson.create();
//        return redissonClient;
//    }
}