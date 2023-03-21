package com.gdou.order.receiver;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdou.order.entity.MqOrder;
import com.gdou.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.gdou.order.constant.MqConstants.ORDER_QUEUE_NAME;


/**
 * @author xzh
 * @time 2023/1/25 23:27
 * 订单入库队列监听
 */
@Component
@RabbitListener(queues = ORDER_QUEUE_NAME)
public class OrderReceiver{

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void process(String messageBody, Message message, Channel channel){
        ObjectMapper mapper = new ObjectMapper();

        MqOrder mqOrder;
        try {
            mqOrder = mapper.readValue(messageBody, MqOrder.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        //执行数据入库
        boolean flag;
        try {
            flag = orderService.saveOrderInfo(mqOrder);
        }catch (Exception e){
            e.printStackTrace();
            flag = false;
        }

        //入库成功 进行签收
        if (flag){
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

}
