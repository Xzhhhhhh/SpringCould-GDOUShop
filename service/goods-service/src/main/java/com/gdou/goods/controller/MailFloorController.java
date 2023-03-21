package com.gdou.goods.controller;


import com.gdou.R;
import com.gdou.goods.entity.vo.FloorVo;
import com.gdou.goods.service.FloorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xzh
 * @since 2022-12-20
 */
@RestController
@RequestMapping("/goods/floor")
public class MailFloorController {

    @Autowired
    private FloorService floorService;

    /**
     * @author xzh
     * @time 2022/12/20 10:47
     * 查询首页楼层数据
     */
    @GetMapping("/list")
    public R getFloorList(){
        List<FloorVo> result =  floorService.getFloorVoList();
        return R.success(result);
    }

}

