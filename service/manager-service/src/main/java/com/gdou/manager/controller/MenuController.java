package com.gdou.manager.controller;


import cn.hutool.core.map.MapUtil;
import com.gdou.R;
import com.gdou.manager.entity.dto.MenuDto;
import com.gdou.manager.service.ManagerService;
import com.gdou.manager.service.MenuService;
import com.gdou.manager.utils.UserUtil;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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
 * @since 2022-12-18
 */
@RestController
@RequestMapping("/manager/menu")
public class MenuController {

    @Autowired
    private ManagerService managerService;
    @Autowired
    private MenuService menuService;

    /**
     * @author xzh
     * @time 2022/12/18 22:03
     * 查询用户菜单
     */
    @GetMapping("/nav")
    public R getUserNav() {

        String userId = UserUtil.getLoginUserId();

        //获取权限集合
        List<String> userAuthorityList = managerService.getManagerAuthorityList(userId);
        //导航栏菜单
        List<MenuDto> currentUserNav = menuService.getCurrentUserNav(userId);
        return R.success(
                MapUtil.builder()
                        .put("nav", currentUserNav)
                        .put("authoritys", userAuthorityList)
                        .map()
        );
    }




}

