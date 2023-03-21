package com.gdou.goods.controller;


import com.gdou.R;
import com.gdou.goods.entity.vo.CategorizeVo;
import com.gdou.goods.service.CategorizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  商城分类查询
 * </p>
 *
 * @author xzh
 * @since 2022-12-20
 */
@RestController
@RequestMapping("/goods/categorize")
public class MailCategorizeController {

    @Autowired
    private CategorizeService categorizeService;


    @GetMapping
    public R getCategorizeList(){
        return R.success(categorizeService.getAllCategorize());
    }

}

