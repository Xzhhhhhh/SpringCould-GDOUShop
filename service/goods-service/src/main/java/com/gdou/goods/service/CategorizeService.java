package com.gdou.goods.service;

import com.gdou.goods.entity.Categorize;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gdou.goods.entity.vo.CategorizeVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xzh
 * @since 2022-12-20
 */
public interface CategorizeService extends IService<Categorize> {

    List<CategorizeVo> getAllCategorize();

}
