package com.gdou.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdou.manager.entity.Manager;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ManagerMapper extends BaseMapper<Manager> {
    List<String> selectPermsByUserId(String userId);
}
