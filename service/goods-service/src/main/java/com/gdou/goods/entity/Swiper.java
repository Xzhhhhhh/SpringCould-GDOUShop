package com.gdou.goods.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author xzh
 * @time 2022/12/19 21:36
 * 轮播图类
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Swiper {

    private Integer id;

    private String imageSrc;

    private String goodsId;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
