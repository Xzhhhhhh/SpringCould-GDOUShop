package com.gdou.goods.testEntity;

import lombok.Data;

@Data
public class TestPic {

    private Integer pics_id;

    private Integer goods_id;

    private String pics_big;
    private String pics_mid;
    private String pics_sma;
    private String pics_big_url;
    private String pics_mid_url;
    private String pics_sma_url;

}
