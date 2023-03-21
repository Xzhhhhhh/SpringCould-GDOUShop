package com.gdou.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ExecutorConfig {


    @Bean("clearCartExecutors")
    public Executor clearCartExecutors(){
        //创建一个用于处理商品状态不为正常 修改购物车选中状态的线程池
        return new ThreadPoolExecutor(
                2,//核心2个
                10,//最多10个线程
                200L,//200ms
                TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(50),//最多阻塞50个
                new ThreadPoolExecutor.DiscardPolicy());
    }


}
