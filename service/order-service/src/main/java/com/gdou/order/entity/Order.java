package com.gdou.order.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author xzh
 * @time 2022/12/26 19:52
 * 订单表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    /**
     * 订单id
     */
    private Long id;

    /**
     * 微信预支付订单id
     */
    private String wxPrepayOrderId;

    /**
     * 微信支付订单id
     */
    private String wxOrderId;


    /**
     * 用户id
     */
    private String userId;


    /**
     * 支付金额
     */
    private BigDecimal amount;


    /**
     * 订单状态
     */
    private Integer state;

    /**
     * 是否退款
     */
    private Integer isRefund;

    /**
     * 订单类型
     */
    private Integer orderType;


    /**
     *
     * 支付时间
     */
    private Date payTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
