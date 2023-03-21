package com.gdou.cart.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tb_order_message")
public class OrderMessage {

    @TableId(value = "message_id",type = IdType.ASSIGN_ID)
    private Long messageId;

    private String messageBody;

    private Integer status;

    private String routeKey;

    private String exchange;

    private Integer count;

    private Date tryTime;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}
