package com.gdou.order;


import com.gdou.config.MyMetaObjectHandler;
import com.gdou.feign.client.GoodsClient;

import com.gdou.feign.client.UserClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
@EnableFeignClients(clients = { GoodsClient.class, UserClient.class})
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class,args);
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

}
