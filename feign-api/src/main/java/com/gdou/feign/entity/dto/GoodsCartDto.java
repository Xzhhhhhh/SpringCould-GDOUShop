package com.gdou.feign.entity.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsCartDto {

    /**
     * 商品id
     */
    private Integer id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 单个用户单次最大购买数量
     */
    private Integer maxNum;

    /**
     * 商品状态
     */
    private Integer state;

    /**
     * 商品小logo
     */
    private String smallLogo;

    /**
     * 商品价格
     */
    private BigDecimal price;


}
