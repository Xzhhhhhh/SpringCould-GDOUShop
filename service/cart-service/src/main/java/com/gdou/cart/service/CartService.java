package com.gdou.cart.service;

import com.gdou.R;
import com.gdou.cart.entity.Cart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gdou.feign.entity.dto.GoodsOrderDto;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xzh
 * @since 2022-12-21
 */
public interface CartService extends IService<Cart> {

    R updateCartGoodsNum(String userId, int goods_id, int count);

    R saveInCart(String userId, int goods_id);

    R deleteGoodsInCart(String userId, int goods_id);

    R updateCartGoodsCheck(Integer goods_id, Integer check,String userId);

    R cartGoodsFullCheck(Integer check, String userId);

    R getCart(String userId);

    R getCartGoodsNumber(String userId);

    List<GoodsOrderDto> getGoodsCartIdToOrder(String userId);

    R cartGoodsPayment(String userId,String address);

    R goodsSeckill(String userId, Integer seckillGoodsId, String address);
}
