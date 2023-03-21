package com.gdou.feign.entity.dto;


import lombok.Data;

import java.math.BigDecimal;

/**
 * @author xzh
 * @time 2022/12/24 12:10
 * 购物车销售相关信息
 */

@Data
public class GoodsCartSaleDto {


    /**
     * 商品id
     */
    private Integer id;

    /**
     * 单个用户单次最大购买数量
     */
    private Integer maxNum;

    /**
     * 商品单价
     */
    private BigDecimal price;

    /**
     * 商品状态
     */
    private Integer state;

}
