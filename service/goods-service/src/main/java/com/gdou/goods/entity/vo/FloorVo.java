package com.gdou.goods.entity.vo;


import com.gdou.goods.entity.Floor;
import lombok.Data;

import java.util.List;

@Data
public class FloorVo {

    //楼层标题
    private Floor floorTitle;

    //楼层内容列表
    private List<Floor> productList;

}
