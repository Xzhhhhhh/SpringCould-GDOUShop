package com.gdou.goods.service.impl;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gdou.R;
import com.gdou.goods.entity.Goods;
import com.gdou.goods.entity.Picture;
import com.gdou.goods.entity.SeckillGoods;
import com.gdou.goods.entity.vo.SeckillGoodsDetailVo;
import com.gdou.goods.entity.vo.SeckillGoodsListVo;
import com.gdou.goods.service.GoodsService;
import com.gdou.goods.service.PictureService;
import com.gdou.goods.service.SeckillGoodsService;
import com.gdou.goods.mapper.SeckillGoodsMapper;
import com.gdou.goods.utils.MyRedisLock;
import com.gdou.utils.BeanCopyUtil;
import com.gdou.goods.utils.BeanMapUtil;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * @author Lenovo
 * @description 针对表【tb_seckill_goods】的数据库操作Service实现
 * @createDate 2023-02-19 19:47:51
 */
@Service
public class SeckillGoodsServiceImpl extends ServiceImpl<SeckillGoodsMapper, SeckillGoods>
        implements SeckillGoodsService {

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private PictureService pictureService;

    @Autowired
    private RBloomFilter<Integer> seckillGoodsBloomFilter;


    @Autowired
    private MyRedisLock myRedisLock;

    @Autowired
    private Executor seckillGoodsRebuildExecutors;


    /**
     * @author xzh
     * @time 2023/2/19 19:51
     * 将一个商品设置为秒杀商品
     */
    @Override
    public R setGoodsSeckill(SeckillGoods seckillGoods) {
        //判断商品是否存在
        Goods goods = goodsService.getById(seckillGoods.getGoodsId());
        if (goods == null) return R.success("商品不存在！", null);
        LambdaQueryWrapper<SeckillGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillGoods::getGoodsId,seckillGoods.getGoodsId())
                .ge(SeckillGoods::getEndTime,new Date())
                .select(SeckillGoods::getSeckillGoodsId);
        //查看秒杀商品活动是否已经结束
        SeckillGoods seckillGoods1 = this.getOne(wrapper);

        if (null != seckillGoods1) return R.success("商品存在一个未结束的秒杀活动！",false);

        //符合创建秒杀活动
        //查询所有图片
        List<Picture> pictureList = pictureService.list(new LambdaQueryWrapper<Picture>()
                .eq(Picture::getGoodsId, seckillGoods.getGoodsId()));

        //商品存在 封装信息
        seckillGoods.setName(goods.getName());
        seckillGoods.setOriginalPrice(goods.getPrice());
        seckillGoods.setNumber(100);
        seckillGoods.setTotalNumber(100);
        seckillGoods.setIntroduce(goods.getIntroduce());
        seckillGoods.setSmallLogo(goods.getSmallLogo());
        seckillGoods.setPicsJsonStr(JSON.toJSONString(pictureList));

        LocalDateTime localExpireTime = LocalDateTime.now().plusHours(2);
        Date expireTime = Date.from(localExpireTime.atZone(ZoneId.systemDefault()).toInstant());
        seckillGoods.setExpireTime(expireTime);

        //数据库持久化
        this.save(seckillGoods);

        //加入布隆过滤器
        seckillGoodsBloomFilter.add(seckillGoods.getSeckillGoodsId());

        //将id插入zset集合中 方便排序
        String seckillGoodsId = String.valueOf(seckillGoods.getSeckillGoodsId());
        long score = seckillGoods.getEndTime().getTime();
        stringRedisTemplate.boundZSetOps("goods:seckill:zset")
                .add(seckillGoodsId, score);

        //将商品信息插入hashmap中方便修改
        stringRedisTemplate.boundHashOps("goods:seckill:hash:" + seckillGoodsId)
                .putAll(BeanMapUtil.beanToMap(seckillGoods));
        return R.success("添加成功！");
    }

    /**
     * @author xzh
     * @time 2023/2/19 22:09
     * 秒杀商品的分页查询
     */
    @Override
    public Map<Object, Object> search(long current, long size) {
        Set<String> range = stringRedisTemplate.boundZSetOps("goods:seckill:zset")
                .range((current - 1) * size, current * size - 1);

        Long total = stringRedisTemplate.boundZSetOps("goods:seckill:zset").size();

        if (range == null || range.isEmpty())
            return MapUtil.builder().put("total", 0)
                    .put("goods", Collections.EMPTY_LIST)
                    .build();

        List<Object> objects = stringRedisTemplate.executePipelined((RedisCallback<List<SeckillGoods>>) connection -> {
            for (String key : range) {
                connection.hGetAll(("goods:seckill:hash:" + key).getBytes());
            }
            return null;
        });
        String jsonString = JSON.toJSONString(objects);
        List<SeckillGoods> seckillGoodsList = JSON.parseArray(jsonString, SeckillGoods.class);

        List<SeckillGoodsListVo> result = BeanCopyUtil.copyList(seckillGoodsList, SeckillGoodsListVo.class);

        //转成列表Vo集合
        return MapUtil.builder()
                .put("total", total)
                .put("goods", result)
                .build();
    }

    /**
     * @author xzh
     * @time 2023/2/20 11:44
     * 查询秒杀商品详情
     */
    @Override
    public R seckillGoodsDetail(Integer goodsId) {
        //1.布隆过滤器判断是否存在 解决缓存穿透
        //存在此布隆过滤器
        if (seckillGoodsBloomFilter.isExists()){
            //布隆过滤器中不存在
            if(!seckillGoodsBloomFilter.contains(goodsId)){
                return R.success("商品不存在！",false);
            }
        }

        //布隆过滤器中存在
        //2.redis查看是否命中 未命中 则异步线程获取 这里返还旧数据
        SeckillGoods seckillGoods =  querySeckillGoodsWithLogicalExpire(goodsId);
        if (seckillGoods == null){
            return R.success("商品不存在！",false);
        }

        //3.查看秒杀活动是否开始
        if (seckillGoods.getBeginTime().after(new Date())){
            //还未开始
            return R.success("该商品秒杀活动还未开始",false);
        }

        //4.判断是否已经结束x
        if (new Date().after(seckillGoods.getEndTime())){
            //已经结束
            return R.success("该商品秒杀活动已经结束,下次来早点吧",false);
        }

        //4.已经开始 转换结果 封装图片
        SeckillGoodsDetailVo seckillGoodsDetailVo = BeanCopyUtil.copyObject(seckillGoods, SeckillGoodsDetailVo.class);
        seckillGoodsDetailVo.setPics(JSON.parseArray(seckillGoods.getPicsJsonStr(),Picture.class));

        return R.success(seckillGoodsDetailVo);
    }

    private SeckillGoods querySeckillGoodsWithLogicalExpire(Integer goodsId) {
        Map<Object, Object> seckillGoodsMap =
                stringRedisTemplate.opsForHash().entries("goods:seckill:hash:" + goodsId);
        if (seckillGoodsMap.isEmpty()){
            return null;
        }
        String jsonString = JSON.toJSONString(seckillGoodsMap);
        SeckillGoods seckillGoods = JSON.parseObject(jsonString, SeckillGoods.class);
        Date expireTime = seckillGoods.getExpireTime();

        //判断是否已经过期
        if (expireTime.after(new Date())){
            //未过期返回结果
            return seckillGoods;
        }

        //已过期需要进行缓存重建
        String lockKey = "lock:seckill:goods:cacheRebuild:"+goodsId;
        //如果获取锁成功
        if(myRedisLock.tryLock(lockKey)){
            //再一次获取判断是否过期 双端检索
            Map<Object, Object> seckillGoodsMapAgain =
                    stringRedisTemplate.opsForHash().entries("goods:seckill:hash:" + goodsId);
            String jsonStringAgain = JSON.toJSONString(seckillGoodsMapAgain);
            SeckillGoods seckillGoodsAgain = JSON.parseObject(jsonStringAgain, SeckillGoods.class);

            if(seckillGoodsAgain.getExpireTime().after(new Date())){
                //已经未过期
                //释放锁 返回结果
                myRedisLock.unlock(lockKey);
                return seckillGoodsAgain;
            }

            //还是过期 则进行重建
            seckillGoodsRebuildExecutors.execute(()->{
                SeckillGoods SeckillGoodsRebuild = this.getById(goodsId);
                LocalDateTime localExpireTime = LocalDateTime.now().plusHours(2);
                Date RebuildGoodsExpireTime = Date.from(localExpireTime.atZone(ZoneId.systemDefault()).toInstant());
                SeckillGoodsRebuild.setExpireTime(RebuildGoodsExpireTime);
                SeckillGoodsRebuild.setExpireTime(RebuildGoodsExpireTime);

                //缓存重建
                stringRedisTemplate.boundHashOps("goods:seckill:hash:" + goodsId)
                        .putAll(BeanMapUtil.beanToMap(SeckillGoodsRebuild));

                //解锁
                myRedisLock.unlock(lockKey);
            });
        }

        //获取锁失败 返回旧数据
        return seckillGoods;
    }

}


