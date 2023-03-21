package com.gdou.goods.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @TableName tb_seckill_goods
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value ="tb_seckill_goods")
public class SeckillGoods implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 秒杀商品id
     */
    @TableId(type = IdType.AUTO)
    private Integer seckillGoodsId;

    /**
     * 秒杀商品id
     */
    private Integer goodsId;

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
     * 商品描述
     */
    private String introduce;

    /**
     * 商品小logo
     */
    private String smallLogo;


    /**
     * 商品的图片集合的JSON字符串
     */
    private String picsJsonStr;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 开始时间
     */
    private Date beginTime;

    /**
     * 结束时间
     */

    private Date endTime;

    /**
     * redis逻辑过期时间
     */
    @TableField(exist = false)
    private Date expireTime;


}