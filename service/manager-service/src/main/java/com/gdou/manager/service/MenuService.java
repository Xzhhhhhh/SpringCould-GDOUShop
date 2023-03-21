package com.gdou.manager.service;

import com.gdou.manager.entity.Menu;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gdou.manager.entity.dto.MenuDto;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xzh
 * @since 2022-12-18
 */
public interface MenuService extends IService<Menu> {

    List<MenuDto> getCurrentUserNav(String userId);
}
