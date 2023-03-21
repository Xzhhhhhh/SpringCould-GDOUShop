package com.gdou.goods.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class CategorizeVo {

    private Integer id;

    private String name;

    private String icon;

    private List<CategorizeVo> children;

}
