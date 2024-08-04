package com.zourui.springbootinit.config;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

class MyDiscardPolicy implements RejectedExecutionHandler {
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        // 直接丢弃任务
        System.out.println("任务 " + r.toString() + " 拒绝从 " + e.toString());
    }
}