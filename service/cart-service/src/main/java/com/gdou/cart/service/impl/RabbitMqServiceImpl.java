package com.gdou.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdou.cart.entity.Cart;
import com.gdou.cart.entity.MqOrder;
import com.gdou.cart.entity.OrderMessage;
import com.gdou.cart.mapper.OrderMessageMapper;
import com.gdou.cart.service.RabbitMqService;
import com.gdou.feign.entity.dto.GoodsCartDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.gdou.cart.constants.MqConstants.*;


@Slf4j
@Service
public class RabbitMqServiceImpl implements RabbitMqService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OrderMessageMapper orderMessageMapper;

    @Override
    public void asynOrderEnduringAndOrderExpire(String userId, String address, List<Cart> cartGoodsList,
                                                List<GoodsCartDto> goodsCartInfoList, BigDecimal price,
                                                long orderId, String wxPrepayOrderId,
                                                Map<String, Object> map,Integer orderType) {
        asynOrderEnduring(userId, address, cartGoodsList, goodsCartInfoList, price, orderId, wxPrepayOrderId,map,orderType);
        orderExpire(orderId,cartGoodsList,orderType);
    }

    /**
     * @author xzh
     * @time 2023/1/25 13:20
     * 订单过期消息
     */
    private void orderExpire(long orderId,List<Cart> cartGoodsList,Integer orderType){
        //创建Mq订单信息
        MqOrder mqOrder = new MqOrder();
        mqOrder.setCartGoodsList(cartGoodsList);
        mqOrder.setOrderId(orderId);
        mqOrder.setOrderType(orderType);
        ObjectMapper mapper = new ObjectMapper();
        String messageBody;
        try {
            messageBody = mapper.writeValueAsString(mqOrder);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        rabbitTemplate.convertAndSend(ORDER_EXPIRE_EXCHANGE_NAME, ORDER_EXPIRE_QUEUE_ROUTE_KEY,messageBody,
                message -> {
                    message.getMessageProperties().setHeader("x-delay",MESSAGE_ORDER_DELAY_TIME);//订单失效时间30分钟
                    return message;
                });
    }

    /**
     * @author xzh
     * @time 2023/1/25 13:20
     * 订单持久化消息
     */
    private void asynOrderEnduring(String userId, String address, List<Cart> cartGoodsList,
                                   List<GoodsCartDto> goodsCartInfoList,
                                   BigDecimal price, long orderId,
                                   String wxPrepayOrderId,Map<String, Object> map,Integer orderType) {
        //创建Mq订单信息
        MqOrder mqOrder = new MqOrder();
        mqOrder.setOrderType(orderType);
        mqOrder.setOrderId(orderId);
        mqOrder.setAmount(price);
        mqOrder.setAddress(address);
        mqOrder.setWxPrepayOrderId(wxPrepayOrderId);
        mqOrder.setUserId(userId);
        mqOrder.setCartGoodsList(cartGoodsList);
        mqOrder.setGoodsCartInfoList(goodsCartInfoList);
        mqOrder.setPayOrderMap(map);
        mqOrder.setCreateTime(new Date());

        ObjectMapper mapper = new ObjectMapper();
        String messageBody;
        try {
            messageBody = mapper.writeValueAsString(mqOrder);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        //消息异步入库 本地消息表入库
        OrderMessage orderMessage = new OrderMessage();
        orderMessage.setMessageBody(messageBody);
        orderMessage.setCount(0);
        orderMessage.setExchange(ORDER_EXCHANGE_NAME);
        orderMessage.setRouteKey(ORDER_QUEUE_ROUTE_KEY);
        orderMessage.setStatus(MESSAGE_NO_RECEIVE_STATUE);
        orderMessage.setTryTime(new Date(System.currentTimeMillis() + 1000L * 60 * MESSAGE_EXPIRE_TIME));
        orderMessageMapper.insert(orderMessage);
        //消息id
        String messageId = orderMessage.getMessageId().toString();
        //进行消息发送
        rabbitTemplate.convertAndSend(ORDER_EXCHANGE_NAME, ORDER_QUEUE_ROUTE_KEY,
                messageBody, new CorrelationData(messageId));
    }

    /**
     * @author xzh
     * @time 2023/1/26 10:08
     * 定时任务扫描本地消息表 进行订单信息重发 每10s一次
     */
    @Scheduled(cron = "0/10 * * * * ? ")
    public void OrderMessageScheduledTasks(){
        //查询需要发送的集合
        List<OrderMessage> resendMessageList = orderMessageMapper.selectList(new LambdaQueryWrapper<OrderMessage>()
                .eq(OrderMessage::getStatus, MESSAGE_NO_RECEIVE_STATUE)//状态为未接收
                .lt(OrderMessage::getTryTime, new Date()));//并且尝试时间小于当前时间

        resendMessageList.forEach(orderMessage -> {
            Integer count = orderMessage.getCount();
            //如果重试次数大于等于3次 则不再进行发送
            if (count>=3){
                //订单信息发送失败
                orderMessage.setStatus(MESSAGE_FAIL_RECEIVE_STATUE);
                //修改状态
                orderMessageMapper.updateById(orderMessage);
            }else {
                //重试次数+1
                orderMessage.setCount(++count);
                orderMessageMapper.updateById(orderMessage);
                //消息重发
                rabbitTemplate.convertAndSend(orderMessage.getExchange(),orderMessage.getRouteKey(),
                        orderMessage.getMessageBody(),new CorrelationData(orderMessage.getMessageId().toString()));
            }
        });
    }



}
