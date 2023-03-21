package com.gdou.cart.entity.vo;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author xzh
 * @time 2022/12/22 15:26
 * 购物车展示对象
 */
@Data
public class CartVo {

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

    /**
     * 商品数量
     */
    private Integer goodsCount;

    /**
     * 是否被选中 0未选中 1选中
     */
    private Integer check;

    /**
     * 商品修改时间 用于排序
     */
    private Date updateTime;


}
