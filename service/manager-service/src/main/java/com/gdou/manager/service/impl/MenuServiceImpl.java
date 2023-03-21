package com.gdou.manager.service.impl;

import com.gdou.manager.entity.Menu;
import com.gdou.manager.entity.dto.MenuDto;
import com.gdou.manager.mapper.MenuMapper;
import com.gdou.manager.service.MenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xzh
 * @since 2022-12-18
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    /**
     * @author xzh
     * @time 2022/12/18 22:11
     * 获取已经登录的用户的菜单
     */
    @Override
    public List<MenuDto> getCurrentUserNav(String userId) {
        //获取用户的所有菜单Id集合
        List<Integer> menuIds = baseMapper.getNavMenuIds(userId);

        //如果用户没有任何菜单
        if(menuIds.size()==0){
            return Collections.emptyList();
        }

        //封装成树形结构
        List<Menu> menus=buildTreeMenu(this.listByIds(menuIds));

        return convert(menus);
    }

    /**
     * 把list集合转为树形结构的菜单
     * @param menus
     * @return
     */
    private List<Menu> buildTreeMenu(List<Menu> menus){
        List<Menu> finalMenus=new ArrayList<>();
        for(Menu menu:menus){

            //先让每一个菜单寻找自己的孩子
            for(Menu e:menus){
                if(Objects.equals(e.getParentId(), menu.getId())){
                    menu.getChildren().add(e);
                }
            }
            //提取父节点
            if(menu.getParentId()==0L){
                finalMenus.add(menu);
            }
        }
        return  finalMenus;
    }

    /**
     * menu转为menuDto类型
     */
    private List<MenuDto> convert(List<Menu> menus){
        List<MenuDto> menuDtoList=new ArrayList<>();

        menus.forEach(m->{
            MenuDto dto=new MenuDto();
            BeanUtils.copyProperties(m,dto);
            /**
             * 递归调用自身子节点，继续将对象进行转换
             */
            if(m.getChildren().size()>0){
                dto.setChildren(convert(m.getChildren()));
            }
            menuDtoList.add(dto);
        });

        return menuDtoList;
    }
}
