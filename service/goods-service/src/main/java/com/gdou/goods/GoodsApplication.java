package com.gdou.goods;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.gdou.config.MyMetaObjectHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class GoodsApplication {
    public static void main(String[] args) {
        SpringApplication.run(GoodsApplication.class,args);
    }

    /**
     * @author xzh
     * @time 2022/12/18 11:50
     * 注入common包下的字段填充类
     */
    @Bean
    public MyMetaObjectHandler myMetaObjectHandler(){
        return new MyMetaObjectHandler();
    }
    /**
     * @author xzh
     * @time 2022/12/20 19:28
     * 解决分页total为零
     */
    @Bean
    public PaginationInterceptor paginationInterceptor(){
        return new PaginationInterceptor();
    }
}
