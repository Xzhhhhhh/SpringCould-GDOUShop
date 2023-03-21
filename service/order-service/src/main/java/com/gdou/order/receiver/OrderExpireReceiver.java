package com.gdou.order.receiver;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdou.order.entity.MqOrder;
import com.gdou.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.gdou.order.constant.MqConstants.ORDER_EXPIRE_QUEUE_NAME;


/**
 * @author xzh
 * @time 2023/1/25 23:27
 * 订单过期队列监听
 */
@Slf4j
@Component
@RabbitListener(queues = ORDER_EXPIRE_QUEUE_NAME)
public class OrderExpireReceiver {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void process(String messageBody, Message message, Channel channel) {

        ObjectMapper mapper = new ObjectMapper();

        MqOrder mqOrder;
        try {
            mqOrder = mapper.readValue(messageBody, MqOrder.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("接收到消息"+mqOrder);

        //进行订单过期处理
        orderService.orderExpire(mqOrder);

        //进行消息签收
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        //签收货物,非批量
        try {
            channel.basicAck(deliveryTag,false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
