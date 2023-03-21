package com.gdou.goods;

import lombok.Data;

import java.util.List;

@Data
public class test {

    private int cat_id;

    private int cat_pid;

    private String cat_name;

    private int cat_level;

    private String cat_icon;

    private List<test> children;

}
