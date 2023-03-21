package com.gdou.cart.config;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.gdou.cart.constants.MqConstants.*;

/**
 * @author xzh
 * @time 2023/1/25 10:06
 * mq交换机、队列  声明与绑定
 */
@Configuration
public class MqConfig {

    /**
     * @author xzh
     * @time 2023/1/25 10:08
     * 订单入库队列
     */
    @Bean
    public Queue orderQueue(){
        return new Queue(ORDER_QUEUE_NAME,true);
    }

    /**
     * @author xzh
     * @time 2023/1/25 10:09
     * 订单过期的延时队列
     */
    @Bean
    public Queue orderExpireQueue(){
        return new Queue(ORDER_EXPIRE_QUEUE_NAME,true);
    }

    /**
     * 延时队列交换机
     *
     * @return
     */
    @Bean
    public CustomExchange orderExpireDelayExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(ORDER_EXPIRE_EXCHANGE_NAME,
                "x-delayed-message", true, false, args);
    }

    /**
     *  订单异步入库交换机
     */
    @Bean
    public Exchange orderExchange(){
        return new TopicExchange(ORDER_EXCHANGE_NAME,true,false);
    }

    /**
     * @author xzh
     * @time 2023/1/25 10:14
     * 声明异步入库绑定关系
     */
    @Bean
    public Binding bindingOrderQueueToExchange(){
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with(ORDER_QUEUE_ROUTE_KEY).noargs();
    }

    /**
     * @author xzh
     * @time 2023/1/25 10:15
     * 声明订单过期绑定关系
     */
    @Bean
    public Binding bindingOrderExpireQueueToExchange(){
        return BindingBuilder.bind(orderExpireQueue()).to(orderExpireDelayExchange()).with(ORDER_EXPIRE_QUEUE_ROUTE_KEY).noargs();
    }

}

