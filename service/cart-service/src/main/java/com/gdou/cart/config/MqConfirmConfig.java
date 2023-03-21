package com.gdou.cart.config;

import com.gdou.cart.entity.OrderMessage;
import com.gdou.cart.mapper.OrderMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import static com.gdou.cart.constants.MqConstants.MESSAGE_RECEIVE_STATUE;


@Slf4j
@Configuration
public class MqConfirmConfig implements ApplicationContextAware {

    @Autowired
    private OrderMessageMapper orderMessageMapper;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 获取RabbitTemplate对象

        RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);

        // 配置ReturnCallback

       // rabbitTemplate.setReturnCallback(((message, repCode, repTest, exchange, routeKey) -> log.info("消息发送失败")));

        // 设置统一的confirm回调。只要消息到达broker就ack=true

        //mq消息手动处理
        rabbitTemplate.setConfirmCallback((CorrelationData correlationData, boolean ack, String s)->{
            if (correlationData!=null){
                String receiveSuccessMessageId = correlationData.getId();
                if (ack){
                    log.info(receiveSuccessMessageId+":消息发送成功！");
                    //消息发送成功修改 本地消息表的状态
                    OrderMessage updateOrderMessage = new OrderMessage();
                    updateOrderMessage.setMessageId(Long.valueOf(receiveSuccessMessageId));
                    updateOrderMessage.setStatus(MESSAGE_RECEIVE_STATUE);
                    orderMessageMapper.updateById(updateOrderMessage);
                }else {
                    log.info(receiveSuccessMessageId+":消息发送失败！");
                }
            }
        });
    }
}
