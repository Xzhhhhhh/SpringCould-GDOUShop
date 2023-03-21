package com.gdou.manager.mapper;

import com.gdou.manager.entity.Menu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author xzh
 * @since 2022-12-18
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {
    List<Integer> getNavMenuIds(String userId);
}
