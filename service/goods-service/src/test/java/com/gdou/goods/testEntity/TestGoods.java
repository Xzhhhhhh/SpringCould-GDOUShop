package com.gdou.goods.testEntity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TestGoods {

    private Integer goods_id;

    private String goods_name;

    private BigDecimal goods_price;

    private Integer goods_number;

    private String goods_introduce;

    private String goods_big_logo;

    private String goods_small_logo;

    private Integer goods_state;

    private Integer cat_one_id;

    private Integer cat_two_id;

    private Integer cat_three_id;

    private List<TestPic> pics;
}
