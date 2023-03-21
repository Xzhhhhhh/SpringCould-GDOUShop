package com.gdou.goods.controller;


import com.gdou.R;
import com.gdou.goods.entity.SeckillGoods;
import com.gdou.goods.service.SeckillGoodsService;
import com.gdou.goods.service.SwiperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author xzh
 * @time 2022/12/19 21:29
 * 后台管理 商品服务Controller
 */
@RestController
@RequestMapping("/goods/manager")
public class ManagerGoodsController {

    @Autowired
    private SwiperService swiperService;
    @Autowired
    private SeckillGoodsService seckillGoodsService;


    /**
     * @author xzh
     * @time 2022/12/19 21:29
     * 查询所有的轮播图
     */
    @GetMapping("/swiper/list")
    public R getSwiperList(){
        //TODO 改为从Redis中获取
        return R.success(swiperService.list());
    }

    /**
     * @author xzh
     * @time 2022/12/19 22:10
     * 设置一个商品置为轮播图
     */
    @PutMapping("/swiper/{id}")
    public R setSwiperByGoodsId(@PathVariable("id") String goodsId){

        return R.success("添加成功！");
    }

    /**
     * @author xzh
     * @time 2023/2/19 14:24
     * 设置一个商品为促销商品
     */
    @PostMapping("/setGoodsSeckill")
    public R setGoodsSeckill(@RequestBody SeckillGoods seckillGoods){
       return  seckillGoodsService.setGoodsSeckill(seckillGoods);
    }


}
