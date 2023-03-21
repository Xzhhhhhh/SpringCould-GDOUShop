package com.gdou.order.util;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @author xzh
 * @time 2022/12/24 15:32
 * 用于生成订单id
 */
@Component
public class OrderIdUtil {

    //开始的时间戳 2002 08 05
    private static final long PAY_ORDER_ID_BEGIN_TIMESTAMP = 1028476800L;

    //商品订单时间戳 2023 01 01
    private static final long GOODS_ORDER_ID_BEGIN_TIMESTAMP = 1672502400L;

    //商品退款订单时间戳
    private static final long ORDER_REFUND_ID_BEGIN_TIMESTAMP = 1666666666L;

    //位运算的位数
    private static final int COUNT_BITS = 32;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 前32位 为 时间戳  后32位为序列号
     */
    public long nextPayOrderId() {
        //1.生成时间戳
        LocalDateTime now = LocalDateTime.now();

        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - PAY_ORDER_ID_BEGIN_TIMESTAMP;

        //2.生成序列号
        //2.1 获取当前日期精确到天
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));

        //TODO 支付订单id 常量
        long count = stringRedisTemplate.opsForValue().increment("icr:payOrder:" + date);

        //3.拼接并返回
        return timestamp << COUNT_BITS | count;
    }

    /**
     * 前32位 为 时间戳  后32位为序列号
     */
    public long nextGoodsOrderId(Integer number) {
        //1.生成时间戳
        LocalDateTime now = LocalDateTime.now();

        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - GOODS_ORDER_ID_BEGIN_TIMESTAMP;

        //2.生成序列号
        //2.1 获取当前日期精确到天
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        //TODO 商品订单id 常量
        long count = stringRedisTemplate.opsForValue().increment("icr:goods:" + date, number);

        //3.拼接并返回
        return timestamp << COUNT_BITS | count;
    }


    /**
     * 前32位 为 时间戳  后32位为序列号
     */
    public long nextOrderRefundId() {
        //1.生成时间戳
        LocalDateTime now = LocalDateTime.now();

        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - ORDER_REFUND_ID_BEGIN_TIMESTAMP;

        //2.生成序列号
        //2.1 获取当前日期精确到天
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        //TODO 商品订单id 常量
        long count = stringRedisTemplate.opsForValue().increment("icr:refund:" + date);

        //3.拼接并返回
        return timestamp << COUNT_BITS | count;
    }

}
