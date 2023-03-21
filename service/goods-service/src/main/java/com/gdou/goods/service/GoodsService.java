package com.gdou.goods.service;

import com.gdou.R;
import com.gdou.feign.entity.dto.GoodsCartDto;
import com.gdou.feign.entity.dto.GoodsCartSaleDto;
import com.gdou.feign.entity.dto.GoodsOrderDto;
import com.gdou.goods.entity.Goods;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xzh
 * @since 2022-12-20
 */
public interface GoodsService extends IService<Goods> {

    Map searchGoodsByQuery(String keyword, Integer cid, long current, long size);

    R getGoodsDetailById(Integer id);

    GoodsCartSaleDto getGoodsCartSaleInfo(Integer id);

    List<GoodsCartDto> getGoodsCartInfoList(List<Integer> idList);

    List<GoodsCartSaleDto> getGoodsCartSaleInfoList(List<Integer> idList);

    R searchGoodsVoByKeyword(String keyword);

    void goodsNumberDeductionDurable(List<GoodsOrderDto> goodsOrderDtoList,Integer orderType);

    void goodsNumberRollBackDurable(List<GoodsOrderDto> goodsOrderDtoList,Integer orderType);
}
