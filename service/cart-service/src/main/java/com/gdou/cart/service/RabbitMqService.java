package com.gdou.cart.service;

import com.gdou.cart.entity.Cart;
import com.gdou.feign.entity.dto.GoodsCartDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface RabbitMqService {
    void asynOrderEnduringAndOrderExpire(String userId, String address,
                                         List<Cart> cartGoodsList,
                                         List<GoodsCartDto> goodsCartInfoList,
                                         BigDecimal price,
                                         long orderId, String wxPrepayOrderId,
                                         Map<String, Object> map,Integer orderType);
}
