package com.gdou.goods.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdou.R;
import com.gdou.feign.entity.dto.GoodsCartDto;
import com.gdou.feign.entity.dto.GoodsCartSaleDto;
import com.gdou.feign.entity.dto.GoodsOrderDto;
import com.gdou.goods.entity.Goods;
import com.gdou.goods.entity.Picture;
import com.gdou.goods.entity.vo.GoodsDetailVo;
import com.gdou.goods.entity.vo.GoodsSearchVo;
import com.gdou.goods.entity.vo.GoodsVo;
import com.gdou.goods.mapper.GoodsMapper;
import com.gdou.goods.service.GoodsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gdou.goods.service.PictureService;
import com.gdou.goods.service.SeckillGoodsService;
import com.gdou.utils.BeanCopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author xzh
 * @since 2022-12-20
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Autowired
    private PictureService pictureService;

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    /**
     * @author xzh
     * @time 2022/12/20 18:17
     * 分页条件查询商品
     */
    @Override
    public Map<Object, Object> searchGoodsByQuery(String keyword, Integer cid, long current, long size) {

        Page<Goods> page = new Page<>(current, size);
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(keyword), Goods::getName, keyword)
                .eq(null != cid, Goods::getCatThreeId, cid);

        this.page(page, wrapper);

        List<Goods> records = page.getRecords();

        records.forEach(goods -> {
            if (goods.getState() != 2 && goods.getNumber() <= 0) {
                goods.setState(1);//设置为缺货
            }
        });

        List<GoodsVo> goodsVos = Collections.emptyList();

        if (!records.isEmpty()) {
            goodsVos = BeanCopyUtil.copyList(records, GoodsVo.class);
        }

        return MapUtil.builder()
                .put("total", page.getTotal())
                .put("goods", goodsVos)
                .build();
    }

    /**
     * @author xzh
     * @time 2022/12/20 20:07
     * 查询商品详细信息
     */
    @Override
    public R getGoodsDetailById(Integer id) {
        Goods goods = this.getById(id);
        if (null == goods) {
            return R.error("商品不存在！");
        }

        GoodsDetailVo goodsDetailVo = BeanCopyUtil.copyObject(goods, GoodsDetailVo.class);
        //查询所有图片
        List<Picture> pictureList = pictureService.list(new LambdaQueryWrapper<Picture>()
                .eq(Picture::getGoodsId, id));

        goodsDetailVo.setPics(pictureList);

        return R.success(goodsDetailVo);
    }

    /**
     * @author xzh
     * @time 2022/12/22 10:36
     * 查询商品信息 返回给 购物车服务
     */
    @Override
    public GoodsCartSaleDto getGoodsCartSaleInfo(Integer id) {

        Goods goods = this.getById(id);
        if (goods == null) {
            //商品为空
            return null;
        }
        if (goods.getState() != 2 && goods.getNumber() <= 0) {
            goods.setState(1);//设置为缺货
        }
        //不为空 转换对象返回
        return BeanCopyUtil.copyObject(goods, GoodsCartSaleDto.class);
    }

    /**
     * @author xzh
     * @time 2022/12/24 12:17
     * 查询商品购买信息集合
     */
    @Override
    public List<GoodsCartSaleDto> getGoodsCartSaleInfoList(List<Integer> idList) {

        List<Goods> goodsList = this.listByIds(idList);
        goodsList.forEach(goods -> {
            if (goods.getState() != 2 && goods.getNumber() <= 0) {
                goods.setState(1);//设置为缺货
            }
        });
        return BeanCopyUtil.copyList(goodsList, GoodsCartSaleDto.class);
    }

    /**
     * @author xzh
     * @time 2022/12/22 15:19
     * 获取用户购物车商品信息
     */
    @Override
    public List<GoodsCartDto> getGoodsCartInfoList(List<Integer> idList) {
        //进行查询
        List<Goods> goodsList = this.listByIds(idList);
        goodsList.forEach(goods -> {
            if (goods.getState() != 2 && goods.getNumber() <= 0) {
                goods.setState(1);//设置为缺货
            }
        });
        //转换类型并返回
        return BeanCopyUtil.copyList(goodsList, GoodsCartDto.class);
    }

    /**
     * @author xzh
     * @time 2023/1/8 19:47
     * 商品搜索功能
     */
    @Override
    public R searchGoodsVoByKeyword(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return R.success(Collections.emptyList());
        }

        List<Goods> goodsList = this.list(new LambdaQueryWrapper<Goods>().like(Goods::getName, keyword));

        if (goodsList.isEmpty()) {
            return R.success(Collections.emptyList());
        }
        return R.success(BeanCopyUtil.copyList(goodsList, GoodsSearchVo.class));
    }


    @Override
    public void goodsNumberDeductionDurable(List<GoodsOrderDto> goodsOrderDtoList, Integer orderType) {
        //普通商品库存扣减
        if (orderType == 1) {
            for (GoodsOrderDto goodsOrderDto : goodsOrderDtoList) {
                this.update()
                        .setSql("number = number - " + goodsOrderDto.getGoodsCount())
                        .eq("id", goodsOrderDto.getId())
                        .gt("number", 0)
                        .update();
            }
        } else if (orderType == 2) { //秒杀商品库存扣减
            for (GoodsOrderDto goodsOrderDto : goodsOrderDtoList) {
                seckillGoodsService.update()
                        .setSql("number = number - " + goodsOrderDto.getGoodsCount())
                        .eq("seckill_goods_id", goodsOrderDto.getId())
                        .gt("number", 0)
                        .update();
            }
        }

    }

    @Override
    public void goodsNumberRollBackDurable(List<GoodsOrderDto> goodsOrderDtoList,Integer orderType) {
        if (orderType == 1){
            for (GoodsOrderDto goodsOrderDto : goodsOrderDtoList) {
                this.update()
                        .eq("id", goodsOrderDto.getId())
                        .setSql("number = number + " + goodsOrderDto.getGoodsCount())
                        .update();
            }
        }else if (orderType == 2){
            for (GoodsOrderDto goodsOrderDto : goodsOrderDtoList) {
                seckillGoodsService.update()
                        .eq("seckill_goods_id", goodsOrderDto.getId())
                        .setSql("number = number + " + goodsOrderDto.getGoodsCount())
                        .update();
            }
        }

    }
}
