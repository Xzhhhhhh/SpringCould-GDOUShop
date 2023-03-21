package com.gdou.goods.service.impl;

import com.gdou.goods.entity.Floor;
import com.gdou.goods.entity.vo.FloorVo;
import com.gdou.goods.mapper.FloorMapper;
import com.gdou.goods.service.FloorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xzh
 * @since 2022-12-20
 */
@Service
public class FloorServiceImpl extends ServiceImpl<FloorMapper, Floor> implements FloorService {

    /**
     * @author xzh
     * @time 2022/12/20 10:53
     * 获取首页楼层数据
     */
    @Override
    public List<FloorVo> getFloorVoList() {

        //查询所有的楼层数据
        List<Floor> floorList = this.list();
        //创建结果
        List<FloorVo> floorVos = new ArrayList<>();

        for (Floor floor : floorList) {
            //说明是此楼层标题
            if (floor.getParentId()==0){
                FloorVo floorVo = new FloorVo();
                floorVo.setFloorTitle(floor);
                //创建子数据集合
                ArrayList<Floor> childrenList = new ArrayList<>();
                //查询子数据
                for (Floor children : floorList) {
                    //是它子数据
                    if (children.getParentId().equals(floor.getId())){
                        childrenList.add(children);
                    }
                }
                //加入floorVo中
                floorVo.setProductList(childrenList);
                //加入最终的结果集中
                floorVos.add(floorVo);
            }
        }
        return floorVos;
    }
}
