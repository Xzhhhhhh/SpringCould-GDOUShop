package com.gdou.cart.controller;


import com.gdou.R;
import com.gdou.cart.service.CartService;
import com.gdou.feign.entity.dto.GoodsOrderDto;
import com.gdou.utils.UserIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 *  用户购物车
 * </p>
 *
 * @author xzh
 * @since 2022-12-21
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * @author xzh
     * @time 2022/12/22 14:55
     * 查询用户购物车数据
     */
    @GetMapping
    public R getCart(HttpServletRequest request){
        String userId = UserIdUtil.getUserId(request);
        return cartService.getCart(userId);
    }


    /**
     * @author xzh
     * @time 2022/12/21 20:19
     * 修改商品数量
     */
    @PutMapping("/goodsCount")
    public R updateShopCartCount(@RequestParam("id") Integer goods_id ,
                            @RequestParam("count") Integer count,HttpServletRequest request){

        String userId = UserIdUtil.getUserId(request);

        return cartService.updateCartGoodsNum(userId,goods_id,count);
    }

    /**
     * @author xzh
     * @time 2022/12/21 22:09
     * 用户执行加入购物车
     */
    @PostMapping
    public R saveInCart(@RequestParam("id") Integer goods_id,HttpServletRequest request){

        String userId = UserIdUtil.getUserId(request);

        return cartService.saveInCart(userId,goods_id);
    }

    /**
     * @author xzh
     * @time 2022/12/22 9:51
     * 用户将商品从购物车中删除
     */
    @DeleteMapping
    public R deleteGoodsInCart(@RequestParam("id")Integer goods_id,HttpServletRequest request){
        String userId = UserIdUtil.getUserId(request);

        return cartService.deleteGoodsInCart(userId,goods_id);
    }

    /**
     * @author xzh
     * @time 2022/12/22 13:38
     * 修改用户购物车是否被选中
     */
    @PutMapping("/check")
    public R updateCheck(@RequestParam("id")Integer goods_id,@RequestParam("check")Integer check,HttpServletRequest request){
        String userId = UserIdUtil.getUserId(request);
        return cartService.updateCartGoodsCheck(goods_id,check,userId);
    }

    /**
     * @author xzh
     * @time 2022/12/22 14:00
     * 用户进行购物车商品全选 全不选
     */
    @PutMapping("/fullCheck")
    public R cartGoodsFullCheck(@RequestParam("check")Integer check,HttpServletRequest request){
        String userId = UserIdUtil.getUserId(request);
        return cartService.cartGoodsFullCheck(check,userId);
    }


    /**
     * @author xzh
     * @time 2022/12/22 17:36
     * 购物车中的商品数
     */
    @GetMapping("/number")
    public R getCartGoodsNumber(HttpServletRequest request){
        String userId = UserIdUtil.getUserId(request);
        return cartService.getCartGoodsNumber(userId);
    }


    /**
     * @author xzh
     * @time 2022/12/24 14:23
     * 查询用户购物车数据 提供给支付模块
     */
    @GetMapping("/order")
    public List<GoodsOrderDto> getCartToOrder(@RequestParam("userId") String userId){
        return cartService.getGoodsCartIdToOrder(userId);
    }

    /**
     * @author xzh
     * @time 2023/1/14 18:48
     * 对购物车中商品进行结算
     */
    @PostMapping("/payment")
    public R paymentCartGoods(HttpServletRequest request,@RequestBody HashMap<String,String> addressMap){
        String userId = UserIdUtil.getUserId(request);
        String address = String.valueOf(addressMap.get("address"));
        return cartService.cartGoodsPayment(userId,address);
    }

    /**
     * @author xzh
     * @time 2023/2/20 23:57
     * 商品秒杀
     */
    @PostMapping("/seckill")
    public R goodsSeckill(HttpServletRequest request,@RequestBody HashMap<String,String> seckillMap){
        String userId = UserIdUtil.getUserId(request);
        Integer seckillGoodsId = Integer.parseInt(seckillMap.get("seckillGoodsId"));
        String address = seckillMap.get("address");
        return cartService.goodsSeckill(userId,seckillGoodsId,address);
    }


}

