package com.zourui.springbootinit.config;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolExecutorConfig {
    @Bean
    ThreadPoolExecutor threadPoolExecutor() {
        ThreadFactory threadFactory = new ThreadFactory(){  //创建线程,匿名内部类来实现
            private int count = 1;
            @Override
            //接收一个 Runnable 对象作为参数，并返回一个新的 Thread 对象
            public Thread newThread(@NotNull Runnable r) {
               Thread thread = new Thread(r);
               thread.setName("线程" + count);
                // 任务++
                count++;
               return thread;
            }
        };
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                2,
                4,
                100,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(4),
                threadFactory,
                new MyDiscardPolicy()
        );
        //返回创建好的 ThreadPoolExecutor 实例，它将被Spring容器管理
        return threadPoolExecutor;
    }

}
