package com.gdou.cart;

import com.gdou.cart.util.DateStrUtil;

import java.util.Date;

public class Test {
    public static void main(String[] args) {
        Date date = DateStrUtil.DateStrToDate("2023-02-28 16:37:22.412");
        System.out.println(date);
    }
}
