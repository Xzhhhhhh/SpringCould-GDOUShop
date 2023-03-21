package com.gdou.cart;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = CartApplication.class)
public class TestLua {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

        private static final DefaultRedisScript<Long> Test;

    static {
        Test = new DefaultRedisScript<>();
        Test.setLocation(new ClassPathResource("DeductGoodsAmount2.lua"));
        Test.setResultType(Long.class);
    }

    @Test
    public void testListLua(){
        HashMap<Integer, Integer> integerIntegerHashMap = new HashMap<>();
        integerIntegerHashMap.put(1,2);

        Object result = stringRedisTemplate.execute(
                Test,
                Arrays.asList("10000","20000","30062","1000"),
                new Object[]{"99","98","50","1"}
        );
        System.out.println(result);
    }


    private static final DefaultRedisScript<Long> SECKILL_GOODS_AMOUNT_SCRIPT;

    static {

        // 初始化秒杀的lua脚本
        SECKILL_GOODS_AMOUNT_SCRIPT = new DefaultRedisScript<>();
        SECKILL_GOODS_AMOUNT_SCRIPT.setLocation(new ClassPathResource("GoodsSeckill.lua"));
        SECKILL_GOODS_AMOUNT_SCRIPT.setResultType(Long.class);
    }
    @Test
    public void  testSekillLua(){

        //Lua脚本判断秒杀
        Long result = stringRedisTemplate.execute(
                SECKILL_GOODS_AMOUNT_SCRIPT,
                Collections.singletonList(String.valueOf(103)),
                String.valueOf(103),
                "1605475823259283458");
        System.out.println(result);
    }

    @Test
    public void  testAmount(){
        //从Redis中获取商品
        List<Object> redisResult = stringRedisTemplate.boundHashOps("goods:seckill:hash:" + 103)
                .multiGet(Arrays.asList("endTime", "beginTime", "name", "number", "promotionPrice"));

        String price = (String)redisResult.get(4);
        System.out.println(new BigDecimal(price));
    }
}
