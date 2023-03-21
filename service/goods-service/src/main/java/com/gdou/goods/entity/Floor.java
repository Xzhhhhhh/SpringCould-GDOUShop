package com.gdou.goods.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import java.io.Serializable;
import java.util.List;

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
@TableName("tb_floor")
public class Floor implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 首页楼层数据id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 楼层开头id
     */
    private Integer parentId;

    /**
     * 图片路径
     */
    private String imageSrc;

    /**
     * 查询关键字
     */
    private String keyword;

    /**
     * 图片长度
     */
    private Integer imageWidth;

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
