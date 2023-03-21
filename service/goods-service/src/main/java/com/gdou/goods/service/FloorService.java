package com.gdou.goods.service;

import com.gdou.goods.entity.Floor;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gdou.goods.entity.vo.FloorVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xzh
 * @since 2022-12-20
 */
public interface FloorService extends IService<Floor> {

    List<FloorVo> getFloorVoList();

}
