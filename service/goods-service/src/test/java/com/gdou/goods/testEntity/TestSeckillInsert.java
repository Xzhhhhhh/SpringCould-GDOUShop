package com.gdou.goods.testEntity;


import com.alibaba.fastjson.JSON;
import com.gdou.R;
import com.gdou.goods.entity.SeckillGoods;
import com.gdou.goods.service.SeckillGoodsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Random;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestSeckillInsert {

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RBloomFilter<Integer> seckillGoodsBloomFilter;

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void test1() {
        for (int i = 50; i <= 100; i++) {
            SeckillGoods seckillGoods = new SeckillGoods();

            //商品id
            seckillGoods.setGoodsId(i);
            //促销价格
            seckillGoods.setPromotionPrice(new BigDecimal("0.01"));
            //开启时间
            seckillGoods.setBeginTime(new Date());

            //设置过期时间
            LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(new Random().nextInt(500)+1000);
            Date endTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            seckillGoods.setEndTime(endTime);
            R r = seckillGoodsService.setGoodsSeckill(seckillGoods);
            System.out.println(r);

        }

    }

    @Test
    public void test() {
        System.out.println(stringRedisTemplate.boundZSetOps("goods:seckill:zset").range(0, 5));

    }

    @Test
    public void test3(){
        RBloomFilter<Integer> seckillGoodsBloomFilter1 =
                redissonClient.getBloomFilter("SeckillGoodsBloomFilter");
        seckillGoodsBloomFilter1.tryInit(2000,0.01);
    }

    @Test
    public void  test4(){
        Map<Object, Object> seckillGoodsMap =
                stringRedisTemplate.opsForHash().entries("goods:seckill:hash:" + 300);
        System.out.println(seckillGoodsMap.isEmpty());
        String jsonString = JSON.toJSONString(seckillGoodsMap);
        SeckillGoods seckillGoods = JSON.parseObject(jsonString, SeckillGoods.class);
        System.out.println(seckillGoods);
    }

}
