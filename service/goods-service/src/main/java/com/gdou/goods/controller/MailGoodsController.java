package com.gdou.goods.controller;


import com.gdou.R;
import com.gdou.feign.entity.dto.GoodsCartDto;
import com.gdou.feign.entity.dto.GoodsCartSaleDto;
import com.gdou.feign.entity.dto.GoodsOrderDto;
import com.gdou.goods.service.GoodsService;
import com.gdou.goods.service.SeckillGoodsService;
import com.gdou.goods.service.SwiperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author xzh
 * @time 2022/12/19 21:29
 * 前台 商品服务Controller
 */
@RestController
@RequestMapping("/goods")
public class MailGoodsController {

    @Autowired
    private SwiperService swiperService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private SeckillGoodsService seckillGoodsService;


    /**
     * @author xzh
     * @time 2022/12/19 21:29
     * 查询所有的轮播图
     */
    @GetMapping("/swiperList")
    public R getSwiperList() {
        //TODO 改为从Redis中获取
        return R.success(swiperService.list());
    }


    /**
     * @author xzh
     * @time 2022/12/20 18:08
     * 根据条件 进行分页查询商品
     */
    @GetMapping("/search")
    public R searchGoods(@RequestParam("query") String keyword, @RequestParam("cid") Integer cid,
                         @RequestParam("pagenum") long current, @RequestParam("pagesize") long size) {

        return R.success(goodsService.searchGoodsByQuery(keyword, cid, current, size));
    }

    /**
     * @author xzh
     * @time 2022/12/20 19:52
     * 通过id查询一个商品详情
     */
    @GetMapping("/detail")
    public R getGoodsInfo(@RequestParam("id") Integer id) {
        if (null == id) {
            return R.error("商品id不能为空！");
        }
        return goodsService.getGoodsDetailById(id);
    }

    /**
     * @author xzh
     * @time 2022/12/22 10:31
     * 通过id查询商品购买信息 提供给 购物车服务
     */
    @GetMapping("/goodsCartSaleInfo")
    public GoodsCartSaleDto getGoodsCartSaleInfo(@RequestParam("id") Integer id) {
        return goodsService.getGoodsCartSaleInfo(id);
    }

    /**
     * @author xzh
     * @time 2022/12/24 12:15
     * 通过id集合查询商品购买信息 提供给 购物车服务
     */
    @PostMapping("/goodsCartSaleInfoList")
    List<GoodsCartSaleDto> getGoodsCartSaleInfoList(@RequestBody List<Integer> idList) {
        return goodsService.getGoodsCartSaleInfoList(idList);
    }

    /**
     * @author xzh
     * @time 2022/12/22 15:17
     * 通过id集合查询商品信息 提供给 购物车服务
     */
    @PostMapping("/goodsCartInfoList")
    public List<GoodsCartDto> getGoodsCartInfoList(@RequestBody List<Integer> idList) {
        return goodsService.getGoodsCartInfoList(idList);
    }

    /**
     * @author xzh
     * @time 2023/1/8 19:43
     * 商品名称模糊查询
     */
    @GetMapping("/qsearch")
    public R queryGoodsByKeyWord(@RequestParam("keyword") String keyword) {
        return goodsService.searchGoodsVoByKeyword(keyword);
    }

    /**
     * @author xzh
     * @time 2023/2/19 14:42
     * 商品库存扣减 mysql持久化
     */
    @PostMapping("/goodsNumberDeductionDurable")
    public void goodsNumberDeductionDurable(@RequestBody List<GoodsOrderDto> goodsOrderDtoList) {
        goodsService.goodsNumberDeductionDurable(goodsOrderDtoList, 1);
    }

    /**
     * @author xzh
     * @time 2023/2/19 14:42
     * 商品库存扣减 mysql持久化
     */
    @PostMapping("/seckillGoodsNumberDeductionDurable")
    public void seckillGoodsNumberDeductionDurable(@RequestBody List<GoodsOrderDto> goodsOrderDtoList) {
        goodsService.goodsNumberDeductionDurable(goodsOrderDtoList, 2);
    }

    /**
     * @author xzh
     * @time 2023/2/19 18:18
     * 商品库存回滚 mysql持久化
     */

    @PostMapping("/goodsNumberRollBackDurable")
    public void goodsNumberRollBackDurable(@RequestBody List<GoodsOrderDto> goodsOrderDtoList) {
        goodsService.goodsNumberRollBackDurable(goodsOrderDtoList, 1);
    }

    /**
     * @author xzh
     * @time 2023/2/26 20:29
     * 秒杀商品库存回滚 mysql持久化
     */
    @PostMapping("/seckillGoodsNumberRollBackDurable")
    public void seckillGoodsNumberRollBackDurable(@RequestBody List<GoodsOrderDto> goodsOrderDtoList) {
        goodsService.goodsNumberRollBackDurable(goodsOrderDtoList, 2);
    }

    /**
     * @author xzh
     * @time 2023/2/19 22:07
     * 秒杀商品的分页查询
     */
    @GetMapping("/seckill")
    public R seckillGoodsSearch(@RequestParam("pagenum") long current, @RequestParam("pagesize") long size) {
        return R.success(seckillGoodsService.search(current, size));
    }

    /**
     * @author xzh
     * @time 2023/2/20 11:42
     * 查询单个商品的秒杀信息
     */
    @GetMapping("/seckill/detail")
    public R seckillGoodsDetail(@RequestParam("id") Integer id) {
        return seckillGoodsService.seckillGoodsDetail(id);
    }
}
