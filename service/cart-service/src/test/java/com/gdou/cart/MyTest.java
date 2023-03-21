//package com.gdou.cart;
//
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.gdou.cart.entity.Cart;
//import com.gdou.cart.entity.OrderMessage;
//import com.gdou.cart.mapper.OrderMessageMapper;
//import com.gdou.cart.service.CartService;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.data.redis.core.script.DefaultRedisScript;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.Collections;
//import java.util.Date;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static com.gdou.cart.constants.MqConstants.*;
//
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = CartApplication.class)
//public class MyTest {
//
//    @Autowired
//    private CartService cartService;
//
//    @Autowired
//    private StringRedisTemplate stringRedisTemplate;
//
//    @Test
//    public void test1(){
//        List<Cart> CartGoodsList = cartService.list(new LambdaQueryWrapper<Cart>()
//                .eq(Cart::getUserId, "1605475823259283458")
//                .eq(Cart::getIsChecked, 1)
//                .select(Cart::getGoodsId, Cart::getGoodsCount));
//
//        //商品的id集合
//        //商品的id集合
//        List<Integer> idList = CartGoodsList
//                .stream()
//                .mapToInt(Cart::getGoodsId)
//                .boxed()
//                .collect(Collectors.toList());
//
////        List objects = redisTemplate.opsForHash()
////                .multiGet("goods:amount",idList);
////
////        System.out.println(objects);
//
//        idList.add(4565);
//        idList.add(235);
//        idList.add(48465);
//        List<Object> objects = stringRedisTemplate
//                .opsForHash()
//                .multiGet("goods:amount", idList.stream().map(Object::toString).collect(Collectors.toList()));
//
//    }
//
//    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
//
//    static {
//        SECKILL_SCRIPT = new DefaultRedisScript<>();
//        SECKILL_SCRIPT.setLocation(new ClassPathResource("deductGoodsAmount.lua"));
//        SECKILL_SCRIPT.setResultType(Long.class);
//    }
//
//    @Test
//    public void  testLua(){
//        Long result = stringRedisTemplate.execute(
//                SECKILL_SCRIPT,
//                Collections.singletonList("goods:amount"),
//                "115461", "5"
//        );
//        System.out.println(result==0L);
//        System.out.println(result);
//    }
//
//    @Autowired
//    private OrderMessageMapper orderMessageMapper;
//
//    @Test
//    public void testMessage(){
//        OrderMessage orderMessage = new OrderMessage();
//        orderMessage.setMessageBody("tttest");
//        orderMessage.setCount(0);
//        orderMessage.setExchange(ORDER_EXCHANGE_NAME);
//        orderMessage.setRouteKey(ORDER_QUEUE_ROUTE_KEY);
//        orderMessage.setStatus(MESSAGE_NO_RECEIVE_STATUE);
//        //消息超时时间1分钟
//        orderMessage.setTryTime(new Date(System.currentTimeMillis()+ 1000L *60*MESSAGE_EXPIRE_TIME));
//        orderMessageMapper.insert(orderMessage);
//        System.out.println(orderMessage.getMessageId());
//    }
//}
