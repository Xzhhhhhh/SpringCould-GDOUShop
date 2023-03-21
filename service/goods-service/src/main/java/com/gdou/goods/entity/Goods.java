package com.gdou.goods.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author xzh
 * @since 2022-12-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_goods")
public class Goods implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 商品id
     */
    @TableId(value = "id")
    private Integer id;

    /**
     * 商品名称
     */
    private String name;



    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 商品数量
     */
    private Integer number;

    /**
     * 单个用户单次最大购买数量
     */
    private Integer maxNum;

    /**
     * 商品描述
     */
    private String introduce;

    /**
     * 商品大logo
     */
    private String bigLogo;

    /**
     * 商品小logo
     */
    private String smallLogo;

    /**
     * 是否促销中 0 否 1 是
     */
    private Integer isPromote;

    /**
     * 商品状态 0 正常 1缺货 2下架
     */
    private Integer state;

    /**
     * 一级分类id
     */
    private Integer catOneId;

    /**
     * 二级分类id
     */
    private Integer catTwoId;

    /**
     * 三级分类id
     */
    private Integer catThreeId;

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


}
