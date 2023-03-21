package com.gdou.goods.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author xzh
 * @time 2022/12/20 19:43
 * 商品列表展示Vo
 */
@Data
public class GoodsVo {

    private Integer id;

    private String name;

    private Integer state;

    private BigDecimal price;

    private String smallLogo;

}
