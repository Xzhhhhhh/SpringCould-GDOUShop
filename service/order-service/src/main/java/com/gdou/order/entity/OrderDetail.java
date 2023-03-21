package com.gdou.order.entity;

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
 * @since 2022-12-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_order_detail")
public class OrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单详情id
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 订单id
     */
    private Long orderId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 退款id
     */
    private Long refundId;

    /**
     * 微信退款id
     */
    private String wxRefundId;

    /**
     * 快递地址
     */
    private String address;

    /**
     * 商品名称
     */
    @TableField("goods_name")
    private String goodsName;

    /**
     * 商品总价
     */
    private BigDecimal totalPrice;

    /**
     * 商品数量
     */
    private Integer goodsCount;

    /**
     * 商品小图片
     */
    private String smallLogo;

    /**
     * 订单状态 0未支付 1已支付 2已取消
     */
    private Integer state;


    /**
     * 退款时间
     */
    private Date refundTime;

    /**
     * 退款成功时间
     */
    private Date refundSuccessTime;

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
