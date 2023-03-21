package com.gdou.goods.entity;

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
@TableName("tb_picture")
public class Picture implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 商品图片id
     */
    @TableId(value = "id")
    private Integer id;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 大图
     */
    private String picsBig;

    /**
     * 中图
     */
    private String picsMid;

    /**
     * 小图
     */
    private String picsSma;

    /**
     * 大图路径
     */
    private String picsBigUrl;

    /**
     * 中图路径
     */
    private String picsMidUrl;

    /**
     * 小图路径
     */
    private String picsSmaUrl;

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
