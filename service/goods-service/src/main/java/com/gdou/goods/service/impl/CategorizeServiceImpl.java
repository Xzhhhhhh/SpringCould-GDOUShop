package com.gdou.goods.service.impl;

import com.gdou.goods.entity.Categorize;
import com.gdou.goods.entity.vo.CategorizeVo;
import com.gdou.goods.mapper.CategorizeMapper;
import com.gdou.goods.service.CategorizeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author xzh
 * @since 2022-12-20
 */
@Service
public class CategorizeServiceImpl extends ServiceImpl<CategorizeMapper, Categorize> implements CategorizeService {

    /**
     * @author xzh
     * @time 2022/12/20 17:33
     * 查询所有的分类 树形结构
     */
    @Override
    public List<CategorizeVo> getAllCategorize() {

        List<Categorize> categorizeList = this.list();
        List<CategorizeVo> result = new ArrayList<>();
        //所有一级分类
        categorizeList.stream()
                .filter(categorize -> categorize.getLevel() == 0)//所有一级分类
                .forEach(one -> {
                    CategorizeVo oneVo = new CategorizeVo();
                    BeanUtils.copyProperties(one, oneVo);
                    List<CategorizeVo> twoVoList = new ArrayList<>();
                    categorizeList.stream()
                            .filter(categorize -> categorize.getLevel() == 1)//所有二级分类
                            .forEach(two -> {
                                //如果相等
                                if (two.getParentId().equals(one.getId())) {
                                    CategorizeVo twoVo = new CategorizeVo();
                                    BeanUtils.copyProperties(two, twoVo);
                                    List<CategorizeVo> threeVoList = new ArrayList<>();
                                    categorizeList.stream()
                                            .filter(categorize -> categorize.getLevel() == 2)//所有三级分类
                                            .forEach(three -> {
                                                    if (three.getParentId().equals(two.getId())){
                                                        CategorizeVo threeVo = new CategorizeVo();
                                                        BeanUtils.copyProperties(three, threeVo);
                                                        threeVoList.add(threeVo);
                                                    }
                                            });
                                    twoVo.setChildren(threeVoList);
                                    twoVoList.add(twoVo);
                                }
                            });
                    oneVo.setChildren(twoVoList);
                    result.add(oneVo);
                });
        return result;
    }
}
