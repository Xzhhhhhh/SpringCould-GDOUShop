package com.gdou.goods.service;

import com.gdou.R;
import com.gdou.goods.entity.SeckillGoods;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
* @author Lenovo
* @description 针对表【tb_seckill_goods】的数据库操作Service
* @createDate 2023-02-19 19:47:51
*/
public interface SeckillGoodsService extends IService<SeckillGoods> {

    R setGoodsSeckill(SeckillGoods seckillGoods);

    Map<Object, Object> search(long current, long size);

    R seckillGoodsDetail(Integer id);
}
