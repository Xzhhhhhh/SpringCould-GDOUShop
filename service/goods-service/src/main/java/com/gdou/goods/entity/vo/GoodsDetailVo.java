package com.gdou.goods.entity.vo;

import com.gdou.goods.entity.Picture;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
public class GoodsDetailVo {

    /**
     * 商品id
     */
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
     * 图片集合
     */
    private List<Picture> pics;


}
