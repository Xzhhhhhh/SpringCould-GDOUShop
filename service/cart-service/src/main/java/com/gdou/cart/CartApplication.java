package com.gdou.cart;

import com.gdou.config.MyMetaObjectHandler;
import com.gdou.feign.client.GoodsClient;
import com.gdou.feign.client.OrderClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableFeignClients(clients = {GoodsClient.class , OrderClient.class})//调用商品服务
public class CartApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class,args);
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
