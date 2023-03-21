package com.gdou.goods.controller;


import com.gdou.R;
import com.gdou.goods.service.HomeCategorizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xzh
 * @since 2022-12-20
 */
@RestController
@RequestMapping("/goods/homeCategorize")
public class MailHomeCategorizeController {

    @Autowired
    private HomeCategorizeService homeCategorizeService;


    @GetMapping("/list")
    public R getHomeCategorizeList(){
        return R.success(homeCategorizeService.list());
    }

}

