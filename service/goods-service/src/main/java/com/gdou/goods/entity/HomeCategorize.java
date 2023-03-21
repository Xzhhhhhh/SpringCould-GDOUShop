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
@TableName("tb_home_categorize")
public class HomeCategorize implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 首页分类id
     */
      @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 首页分类名称
     */
    private String name;

    /**
     * 首页分类图片路径
     */
    private String imageSrc;

    /**
     * 打开方式
     */
    private String openType;

    /**
     * 打开路径
     */
    private String url;

    /**
     * 是否禁用
     */
    private Integer isDisable;

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
