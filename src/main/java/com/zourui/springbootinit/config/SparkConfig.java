package com.zourui.springbootinit.config;

import io.github.briqt.spark4j.SparkClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SparkConfig {
    @Bean
    public SparkClient createClient(){
        SparkClient sparkClient=new SparkClient();
        // 设置认证信息
        sparkClient.appid="f9f4c0fa";
        sparkClient.apiKey="cad6900610065768675afafd85b05923";
        sparkClient.apiSecret="NzNmMjhjMTlmM2I2OWJjNTMyYTI0NjIx";
        return sparkClient;
    }
}
