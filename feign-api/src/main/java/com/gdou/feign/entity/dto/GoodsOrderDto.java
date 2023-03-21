package com.gdou.feign.entity.dto;

import lombok.Data;

@Data
public class GoodsOrderDto {

    /**
     * 商品id
     */
    private Integer id;

    /**
     * 商品购买数量
     */
    private Integer goodsCount;

}
