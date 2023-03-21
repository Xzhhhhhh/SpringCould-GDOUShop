package com.gdou.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdou.order.entity.Cart;
import com.gdou.order.entity.OrderDetail;
import com.gdou.order.util.WxPayUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.*;


@RunWith(SpringRunner.class)
@SpringBootTest
public class test {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void test() {
        List<Object> redisResult = stringRedisTemplate.boundHashOps("goods:seckill:hash:" + 103)
                .multiGet(Arrays.asList("endTime", "beginTime", "name", "number", "promotionPrice"));
        System.out.println(redisResult);
        System.out.println(Integer.valueOf((String) redisResult.get(3)));
        List<Object> redisResult2 = stringRedisTemplate.boundHashOps("goods:seckill:hash:" + 999)
                .multiGet(Arrays.asList("endTime", "beginTime", "name", "number", "promotionPrice"));
        System.out.println(redisResult2);
    }

    @Test
    public void rollbackGoodsAmount() {
        List<Cart> deductAmountSuccessList = new ArrayList<>();
        Cart cart1 = new Cart();
        cart1.setGoodsId(260);
        cart1.setGoodsCount(1);
        deductAmountSuccessList.add(cart1);

        Cart cart2 = new Cart();
        cart2.setGoodsId(59);
        cart2.setGoodsCount(2);
        deductAmountSuccessList.add(cart2);

        Cart cart3 = new Cart();
        cart3.setGoodsId(90);
        cart3.setGoodsCount(3);
        deductAmountSuccessList.add(cart3);

        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Cart deductAmountSuccessGoods : deductAmountSuccessList) {
                connection.hashCommands().hIncrBy("goods:amount".getBytes(),
                        deductAmountSuccessGoods.getGoodsId().toString().getBytes(),
                        deductAmountSuccessGoods.getGoodsCount());
            }
            return null;
        });
    }

    @Test
    public void test1() {
        String data = "{" +
                "    \"transaction_id\":\"1217752501201407033233368018\"," +
                "    \"amount\":{" +
                "        \"payer_total\":100," +
                "        \"total\":100," +
                "        \"currency\":\"CNY\"," +
                "        \"payer_currency\":\"CNY\"" +
                "    }," +
                "    \"mchid\":\"1230000109\"," +
                "    \"trade_state\":\"SUCCESS\"," +
                "    \"bank_type\":\"CMC\"," +
                "    \"promotion_detail\":[" +
                "        {" +
                "            \"amount\":100," +
                "            \"wechatpay_contribute\":0," +
                "            \"coupon_id\":\"109519\"," +
                "            \"scope\":\"GLOBAL\"," +
                "            \"merchant_contribute\":0," +
                "            \"name\":\"单品惠-6\"," +
                "            \"other_contribute\":0," +
                "            \"currency\":\"CNY\"," +
                "            \"stock_id\":\"931386\"," +
                "            \"goods_detail\":[" +
                "                {" +
                "                    \"goods_remark\":\"商品备注信息\"," +
                "                    \"quantity\":1," +
                "                    \"discount_amount\":1," +
                "                    \"goods_id\":\"M1006\"," +
                "                    \"unit_price\":100" +
                "                }," +
                "                {" +
                "                    \"goods_remark\":\"商品备注信息\"," +
                "                    \"quantity\":1," +
                "                    \"discount_amount\":1," +
                "                    \"goods_id\":\"M1006\"," +
                "                    \"unit_price\":100" +
                "                }" +
                "            ]" +
                "        }," +
                "        {" +
                "            \"amount\":100," +
                "            \"wechatpay_contribute\":0," +
                "            \"coupon_id\":\"109519\"," +
                "            \"scope\":\"GLOBAL\"," +
                "            \"merchant_contribute\":0," +
                "            \"name\":\"单品惠-6\"," +
                "            \"other_contribute\":0," +
                "            \"currency\":\"CNY\"," +
                "            \"stock_id\":\"931386\"," +
                "            \"goods_detail\":[" +
                "                {" +
                "                    \"goods_remark\":\"商品备注信息\"," +
                "                    \"quantity\":1," +
                "                    \"discount_amount\":1," +
                "                    \"goods_id\":\"M1006\"," +
                "                    \"unit_price\":100" +
                "                }," +
                "                {" +
                "                    \"goods_remark\":\"商品备注信息\"," +
                "                    \"quantity\":1," +
                "                    \"discount_amount\":1," +
                "                    \"goods_id\":\"M1006\"," +
                "                    \"unit_price\":100" +
                "                }" +
                "            ]" +
                "        }" +
                "    ]," +
                "    \"success_time\":\"2018-06-08T10:34:56+08:00\"," +
                "    \"payer\":{" +
                "        \"openid\":\"oUpF8uMuAJO_M2pxb1Q9zNjWeS6o\"" +
                "    }," +
                "    \"out_trade_no\":\"1217752501201407033233368018\"," +
                "    \"appid\":\"wxd678efh567hg6787\"," +
                "    \"trade_state_desc\":\"支付成功\"," +
                "    \"trade_type\":\"MICROPAY\"," +
                "    \"attach\":\"自定义数据\"," +
                "    \"scene_info\":{" +
                "        \"device_id\":\"013467007045764\"" +
                "    }" +
                "}";

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode decryptData = null;
        try {
            decryptData = objectMapper.readTree(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        //获取详情
        String orderId = String.valueOf(decryptData.get("out_trade_no")).replace("\"", "");
        String wxOrderId = String.valueOf(decryptData.get("transaction_id")).replace("\"", "");
        String tradeState = String.valueOf(decryptData.get("trade_state")).replace("\"", "");


    }

    @Test
    public void test2() {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setTotalPrice(new BigDecimal("0.03"));
        WxPayUtil.orderRefund("2768619902792106007", "465435465643513515",
                "测试", "6", orderDetail);
    }


    private static final DefaultRedisScript<Long> Test;

    static {
        Test = new DefaultRedisScript<>();
        Test.setLocation(new ClassPathResource("TestLua.lua"));
        Test.setResultType(Long.class);
    }


}
