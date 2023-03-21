package com.gdou.manager.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author xzh
 * @time 2022/12/18 13:38
 * 管理员登录信息详情存储线程池
 */

@Configuration
public class ExecutorConfig{
    /**
     * 核心线程
     */
    private final static int corePoolSize = 1;
    /**
     * 最大线程
     */
    private final static int maxPoolSize = 2;
    /**
     * 队列容量
     */
    private final static int queueCapacity=10;

    /**
     * 保持时间
     */
    private final static int keepAliveSeconds = 10;
    /**
     * 名称前缀
     */
    private final static String preFix = "manager:login:info";

    @Bean("ManagerLoginInfoExecutor")
    public Executor myExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix(preFix);
        //当大于最大线程数时 抛出异常
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}