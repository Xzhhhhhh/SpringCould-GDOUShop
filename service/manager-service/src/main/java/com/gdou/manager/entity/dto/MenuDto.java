package com.gdou.manager.entity.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuDto {

    private int id;

    private String name;

    private String title;

    private String icon;

    private String path;

    private int isHidden;

    private  String component;

    List<MenuDto> children = new ArrayList<>();

}
