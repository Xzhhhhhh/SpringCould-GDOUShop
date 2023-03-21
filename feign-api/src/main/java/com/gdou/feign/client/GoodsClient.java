package com.gdou.feign.client;


import com.gdou.feign.entity.dto.GoodsCartDto;
import com.gdou.feign.entity.dto.GoodsCartSaleDto;
import com.gdou.feign.entity.dto.GoodsOrderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("goods-service")
public interface GoodsClient {

    @GetMapping("/goods/goodsCartSaleInfo")
    GoodsCartSaleDto getGoodsCartSaleInfo(@RequestParam("id") Integer id);

    @PostMapping("/goods/goodsCartSaleInfoList")
    List<GoodsCartSaleDto> getGoodsCartSaleInfoList(@RequestBody List<Integer> idList);

    @PostMapping("/goods/goodsCartInfoList")
    List<GoodsCartDto> getGoodsCartInfoList(@RequestBody List<Integer> idList);

    @PostMapping("/goods/goodsNumberDeductionDurable")
    void goodsNumberDeductionDurable(@RequestBody List<GoodsOrderDto> goodsOrderDtoList);


    @PostMapping("/goods/seckillGoodsNumberDeductionDurable")
    void seckillGoodsNumberDeductionDurable(@RequestBody List<GoodsOrderDto> goodsOrderDtoList);

    @PostMapping("/goods/goodsNumberRollBackDurable")
    void goodsNumberRollBackDurable(@RequestBody List<GoodsOrderDto> goodsOrderDtoList);

    @PostMapping("/goods/seckillGoodsNumberRollBackDurable")
    void seckillGoodsNumberRollBackDurable(@RequestBody List<GoodsOrderDto> goodsOrderDtoList);

}
