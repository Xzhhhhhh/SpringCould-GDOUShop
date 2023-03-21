//package com.gdou.goods;
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.gdou.goods.entity.Goods;
//import com.gdou.goods.service.GoodsService;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.List;
//
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class RedisDataTest {
//
//    @Autowired
//    private StringRedisTemplate stringRedisTemplate;
//
//    @Autowired
//    private GoodsService goodsService;
//
//
//    @Test
//    public void addGoodsNum(){
//        List<Goods> goodsList = goodsService.list(new LambdaQueryWrapper<Goods>()
//                .select(Goods::getNumber, Goods::getId));
//       // System.out.println(goodsList);
//        goodsList.forEach(goods -> {
//            stringRedisTemplate.opsForHash()
//                    .put("goods:amount",goods.getId().toString(),goods.getNumber().toString());
//        });
//
//
//    }
//
//}
