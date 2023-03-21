package com.gdou.goods.entity.vo;

import com.gdou.goods.entity.Picture;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class SeckillGoodsListVo {

    private Integer seckillGoodsId;

    /**
     * 秒杀商品名称
     */
    private String name;

    /**
     * 促销价格
     */
    private BigDecimal promotionPrice;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 促销数量
     */
    private Integer number;

    /**
     * 促销总数量
     */
    private Integer totalNumber;

    /**
     * 商品小logo
     */
    private String smallLogo;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 开始时间
     */
    private Date beginTime;

    /**
     * 结束时间
     */
    private Date endTime;


}
