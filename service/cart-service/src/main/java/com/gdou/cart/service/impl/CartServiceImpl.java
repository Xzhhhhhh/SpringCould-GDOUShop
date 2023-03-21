package com.gdou.cart.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gdou.R;
import com.gdou.cart.entity.Cart;
import com.gdou.cart.entity.vo.CartVo;
import com.gdou.cart.mapper.CartMapper;
import com.gdou.cart.service.CartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gdou.cart.service.RabbitMqService;
import com.gdou.cart.util.DateStrUtil;
import com.gdou.cart.util.OrderIdUtil;
import com.gdou.feign.client.GoodsClient;
import com.gdou.feign.client.OrderClient;
import com.gdou.feign.entity.dto.GoodsCartDto;
import com.gdou.feign.entity.dto.GoodsCartSaleDto;
import com.gdou.feign.entity.dto.GoodsOrderDto;
import com.gdou.utils.BeanCopyUtil;
import org.apache.commons.lang.time.DateUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataUnit;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author xzh
 * @since 2022-12-21
 */
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {


    //用于避免商品多次加入购物车的锁
    //private static final Lock saveCartGoodsLock = new ReentrantLock();

    //修改为Redisson锁 一次只锁一个id 确保多个用户可以同时将商品加入购物车
    @Resource
    private RedissonClient redissonClient;
    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private OrderIdUtil orderIdUtil;
    @Autowired
    private OrderClient orderClient;
    @Autowired
    private RabbitMqService rabbitMqService;

    private static final DefaultRedisScript<Long> DEDUCT_GOODS_AMOUNT_SCRIPT;

    private static final DefaultRedisScript<Long> SECKILL_GOODS_AMOUNT_SCRIPT;

    static {
        // 初始化扣减内存的Lua脚本
        DEDUCT_GOODS_AMOUNT_SCRIPT = new DefaultRedisScript<>();
        DEDUCT_GOODS_AMOUNT_SCRIPT.setLocation(new ClassPathResource("DeductGoodsAmount2.lua"));
        DEDUCT_GOODS_AMOUNT_SCRIPT.setResultType(Long.class);

        // 初始化秒杀的lua脚本
        SECKILL_GOODS_AMOUNT_SCRIPT = new DefaultRedisScript<>();
        SECKILL_GOODS_AMOUNT_SCRIPT.setLocation(new ClassPathResource("GoodsSeckill.lua"));
        SECKILL_GOODS_AMOUNT_SCRIPT.setResultType(Long.class);
    }

    /**
     * @author xzh
     * @time 2022/12/21 21:58
     * 修改购物车中商品数量
     */
    @Override
    public R updateCartGoodsNum(String userId, int goods_id, int count) {

        //判断商品是否存在
        GoodsCartSaleDto goodsSaleInfo = goodsClient.getGoodsCartSaleInfo(goods_id);

        if (null == goodsSaleInfo) {
            return R.success("商品不存在", false);
        }

        //商品存在 判断是否下架 或 缺货
        //TODO 商品状态常量
        if (goodsSaleInfo.getState() == 1) {
            //缺货
            return R.success("商品缺货，无法修改购物车数量", false);
        } else if (goodsSaleInfo.getState() == 2) {
            //下架
            return R.success("商品已下架，无法修改购物车数量", false);
        }

        //商品状态正常 取出购物车一次最多购买量
        Integer maxNum = goodsSaleInfo.getMaxNum();

        //判断赋值量是否大于一次最多购买量
        if (maxNum < count) {
            return R.success("不可超过单次最大购买量" + maxNum, false);
        }

        //不大于单次最大购买量 则进行赋值
        //判断是否存在
        LambdaQueryWrapper<Cart> existWrapper = new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId)
                .eq(Cart::getGoodsId, goods_id);
        if (this.getOne(existWrapper) == null) {
            //不存在 说明已经删除
            return R.success("购物车中已不存在此商品！", false);
        }

        //存在 说明可以执行修改
        boolean success = this.update().eq("user_id", userId)
                .eq("goods_id", goods_id)
                .set("goods_count", count).update();
        if (!success) {
            return R.success("修改数量失败！", false);
        }
        return R.success("成功！");
    }

    /**
     * @author xzh
     * @time 2022/12/21 22:12
     * 用户将商品存入 购物车 如果已经存在 则 对其count+1
     */
    @Override
    public R saveInCart(String userId, int goods_id) {
        //判断商品是否存在
        GoodsCartSaleDto goodsSaleInfo = goodsClient.getGoodsCartSaleInfo(goods_id);

        if (null == goodsSaleInfo) {
            return R.success("商品不存在", false);
        }

        //商品存在 判断是否下架 或 缺货
        //TODO 商品状态常量
        if (goodsSaleInfo.getState() == 1) {
            //缺货
            return R.success("商品缺货，加入购物车失败", false);
        } else if (goodsSaleInfo.getState() == 2) {
            //下架
            return R.success("商品已下架，加入购物车失败", false);
        }

        //商品状态正常 取出购物车一次最多购买量
        Integer maxNum = goodsSaleInfo.getMaxNum();


        //判断是否存在于购物车
        LambdaQueryWrapper<Cart> existWrapper = new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getGoodsId, goods_id);

        Cart cart = this.getOne(existWrapper);
        if (cart != null) {

            //已经存在购物车中 对其数量+1 并且标注为已经被选中
            boolean success = this.update().eq("id", cart.getId())
                    .set("is_checked", 1)//设置被选中
                    .set("update_time", new Date())//手动加入修改时间
                    .setSql("goods_count = goods_count + 1") //set goods_count = goods_count + 1
                    .lt("goods_count", maxNum).update();// where id = ? and goods_count < maxNum

            if (!success) {
                //添加失败 超过了购买上限
                return R.success("加入购物车失败！超出了上限" + maxNum, false);
            }

            //加入成功
            return R.success("成功！");
        }
        //加锁 如果已经被获取 则直接返回成功
        //TODO 购物车锁常量
        RLock lock = redissonClient.getLock("lock:cart:" + userId);
        if (lock.tryLock()) {
            //获取锁成功！
            try {
                //购物车不存在此商品
                cart = new Cart().setGoodsId(goods_id)
                        .setUserId(userId)
                        .setIsChecked(1)//设置已被选中
                        .setGoodsCount(1);//设置数量为1

                this.save(cart);
            } finally {
                //释放锁
                lock.unlock();
            }
        }
        return R.success("成功！");
    }

    /**
     * @author xzh
     * @time 2022/12/22 9:52
     * 在购物车中删除一个商品
     */
    @Override
    public R deleteGoodsInCart(String userId, int goods_id) {

        LambdaQueryWrapper<Cart> deleteWrapper = new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getGoodsId, goods_id);

        boolean success = this.remove(deleteWrapper);
        if (!success) {
            return R.success("删除失败！");
        }
        return R.success("成功！");
    }

    /**
     * @author xzh
     * @time 2022/12/22 13:52
     * 修改一个商品是否被选中
     */
    @Override
    public R updateCartGoodsCheck(Integer goods_id, Integer check, String userId) {

        //判断商品是否存在
        GoodsCartSaleDto goodsSaleInfo = goodsClient.getGoodsCartSaleInfo(goods_id);

        if (null == goodsSaleInfo) {
            return R.success("商品不存在", false);
        }

        //商品存在 判断是否下架 或 缺货
        //TODO 商品状态常量
        if (goodsSaleInfo.getState() == 1) {
            //缺货
            return R.success("商品缺货，无法选中", false);
        } else if (goodsSaleInfo.getState() == 2) {
            //下架
            return R.success("商品已下架，无法选中", false);
        }

        //不大于单次最大购买量 则进行赋值
        //判断是否存在
        LambdaQueryWrapper<Cart> existWrapper = new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId)
                .eq(Cart::getGoodsId, goods_id);
        if (this.getOne(existWrapper) == null) {
            //不存在 说明已经删除
            return R.success("购物车中已不存在此商品！", false);
        }

        //存在进行修改选中状态
        boolean success = this.update().eq("user_id", userId).eq("goods_id", goods_id)
                .set("is_checked", check).update();

        if (!success) {
            return R.success("修改选中失败！", false);
        }

        return R.success("成功！");
    }

    /**
     * @author xzh
     * @time 2022/12/22 14:08
     * 商品进行全选或全不选
     */
    @Override
    public R cartGoodsFullCheck(Integer check, String userId) {
        //对与状态正常的商品可以进行全选
        //查询用户购物车中的商品中的销售状态
        List<Cart> list = this.list(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .select(Cart::getGoodsId));

        //购物车中所有商品都为空
        if (list.isEmpty()) {
            return R.success("购物车空空如也！", false);
        }

        List<Integer> idList = list.stream().mapToInt(Cart::getGoodsId).boxed().collect(Collectors.toList());
        List<GoodsCartSaleDto> goodsCartSaleInfoList = goodsClient.getGoodsCartSaleInfoList(idList);

        //满足选中条件的商品id
        List<Integer> canCheckIdList = goodsCartSaleInfoList.stream().filter(goodsCartSaleDto -> goodsCartSaleDto.getState() == 0)
                .mapToInt(GoodsCartSaleDto::getId)
                .boxed()
                .collect(Collectors.toList());

        //购物车中所有符合条件的商品都为空
        if (canCheckIdList.isEmpty()) {
            return R.success("成功！");
        }
        boolean success = this.update().eq("user_id", userId)
                .in("goods_id", canCheckIdList)
                .set("is_checked", check).update();
        if (!success) {
            return R.success("失败！", false);
        }
        return R.success("成功！");
    }

    /**
     * @author xzh
     * @time 2022/12/22 15:01
     * 获取用户购物车数据
     */
    @Override
    public R getCart(String userId) {
        //查询用户购物车中所有商品id
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId);
        List<Cart> cartList = this.list(queryWrapper);
        //如果集合是空的 则返回空数据 和 0
        if (cartList.isEmpty()) {
            return R.success(MapUtil.builder()
                    .put("goodsList", Collections.emptyList())
                    .put("sum", 0)
                    .put("checkTotal", 0)
                    .put("total", 0)
                    .build());
        }
        //转换成id集合
        List<Integer> goodsIdList = cartList.stream()
                .mapToInt(Cart::getGoodsId)
                .boxed().collect(Collectors.toList());

        //通过商品id查询详细信息
        List<GoodsCartDto> goodsCartInfoList = goodsClient.getGoodsCartInfoList(goodsIdList);

        //转化为CartVo对象
        List<CartVo> cartVoList = BeanCopyUtil.copyList(goodsCartInfoList, CartVo.class);

        //待置位为未选中的商品id集合
        ArrayList<Integer> updateCheckIdList = new ArrayList<>();

        //缺货商品集合
        ArrayList<CartVo> noStockGoodsList = new ArrayList<>();

        //下架商品集合
        ArrayList<CartVo> offSaleGoodsList = new ArrayList<>();

        Iterator<CartVo> iterator = cartVoList.iterator();

        while (iterator.hasNext()) {
            CartVo cartVo = iterator.next();
            //对购物车数据库中的进行遍历 封装是否选择 和 数量
            for (Cart cart : cartList) {
                if (cartVo.getId().equals(cart.getGoodsId())) {
                    cartVo.setGoodsCount(cart.getGoodsCount());
                    cartVo.setCheck(cart.getIsChecked());
                    cartVo.setUpdateTime(cart.getUpdateTime());
                }
            }
            //商品缺货
            if (cartVo.getState() == 1) {
                //加入缺货集合
                noStockGoodsList.add(cartVo);
                //从正常购物车列表中删除
                iterator.remove();
                //已经被选中 加入被置位未选中集合中
                if (cartVo.getCheck() == 1) {
                    cartVo.setCheck(0);
                    updateCheckIdList.add(cartVo.getId());
                }
            } else if (cartVo.getState() == 2) {
                //加入下架集合
                offSaleGoodsList.add(cartVo);
                //从正常购物车列表中删除
                iterator.remove();
                //已经被选中 加入被置位未选中集合中
                if (cartVo.getCheck() == 1) {
                    cartVo.setCheck(0);
                    updateCheckIdList.add(cartVo.getId());
                }
            }
        }

        //对下架和缺货商品置为未选中
        if (updateCheckIdList.size() != 0) {
            this.update().eq("user_id", userId)//用户id
                    .in("goods_id", updateCheckIdList)//商品id
                    .set("is_checked", 0).update();//设置为未选中
        }

        //计算已经选择的商品的总价
        BigDecimal sum = new BigDecimal(0);//总价格
        int checkTotal = 0;//选中的总数量
        int total = 0;//总数量
        for (CartVo cartVo : cartVoList) {
            //TODO 常量
            if (cartVo.getCheck() == 1) {
                sum = sum.add(cartVo.getPrice().multiply(new BigDecimal(cartVo.getGoodsCount())));
                //上架且 选中的数量
                checkTotal += cartVo.getGoodsCount();
            }
            //所有上架的数量
            total += cartVo.getGoodsCount();
        }

        //对购物车数据进行通过修改时间进行降序处理
        cartVoList = cartVoList.stream()
                .sorted((o1, o2) -> o1.getUpdateTime().after(o2.getUpdateTime()) ? -1 : 1)
                .collect(Collectors.toList());

        return R.success(MapUtil.builder()
                .put("noStockGoodsList", noStockGoodsList)
                .put("offSaleGoodsList", offSaleGoodsList)
                .put("goodsList", cartVoList)
                .put("sum", sum)
                .put("checkTotal", checkTotal)
                .put("total", total)//减去因为下架而无法选中的商品数量
                .build());
    }


    /**
     * @author xzh
     * @time 2022/12/22 17:38
     * 获取购物车中的商品数
     */
    @Override
    public R getCartGoodsNumber(String userId) {

        List<Cart> list = this.list(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .select(Cart::getGoodsCount, Cart::getGoodsId));
        if (list.isEmpty()) {
            return R.success(0);
        }
        //并且此商品状态必须为正常销售
        //调用商品服务查询 商品状态
        List<Integer> idList = list.stream().mapToInt(Cart::getGoodsId).boxed().collect(Collectors.toList());
        List<GoodsCartSaleDto> goodsCartSaleInfoList = goodsClient.getGoodsCartSaleInfoList(idList);

        int sum = 0;
        for (GoodsCartSaleDto goodsCartSaleDto : goodsCartSaleInfoList) {
            for (Cart goodsCart : list) {
                if (goodsCart.getGoodsId().equals(goodsCartSaleDto.getId())
                        && goodsCartSaleDto.getState() == 0) {
                    sum += goodsCart.getGoodsCount();
                }
            }
        }
        return R.success(sum);
    }

    /**
     * @author xzh
     * @time 2022/12/24 14:39
     * 支付模块获取购物车数据
     */
    @Override
    public List<GoodsOrderDto> getGoodsCartIdToOrder(String userId) {
        //获取所有选中的商品Id给支付模块
        List<Cart> cartList = this.list(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getIsChecked, 1)//已经选中
                .select(Cart::getGoodsId, Cart::getGoodsCount));//获取商品id

        List<GoodsOrderDto> GoodsOrderDtoList = new ArrayList<>();
        for (Cart cart : cartList) {
            GoodsOrderDto goodsOrderDto = new GoodsOrderDto();
            goodsOrderDto.setId(cart.getGoodsId());
            goodsOrderDto.setGoodsCount(cart.getGoodsCount());
            GoodsOrderDtoList.add(goodsOrderDto);
        }
        return GoodsOrderDtoList;
    }


    /**
     * @author xzh
     * @time 2023/1/23 22:08
     * 用户对选中商品进行创建订单
     */
    @Override
    public R cartGoodsPayment(String userId, String address) {

        //查询用户购物车中选中的商品id 和 购买数量
        List<Cart> cartGoodsList = this.list(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getIsChecked, 1)
                .select(Cart::getGoodsId, Cart::getGoodsCount));

        //如果选中商品集合为空则返回创建失败
        if (cartGoodsList.isEmpty()) {
            return R.success("购物车没有选中的商品！", false);
        }
        //商品的id集合
        List<Integer> idList = cartGoodsList
                .stream()
                .mapToInt(Cart::getGoodsId)
                .boxed()
                .collect(Collectors.toList());


        //存在选中的商品 查询商品详情
        List<GoodsCartDto> goodsCartInfoList = goodsClient.getGoodsCartInfoList(idList);


        //商品库存集合
        List<Object> goodsAmountList = stringRedisTemplate
                .opsForHash()
                .multiGet("goods:amount", idList.stream().map(Object::toString).collect(Collectors.toList()));


        //需支付的总金额
        BigDecimal price = new BigDecimal(0);


        //判断每个商品状态 、 库存 、 单次购买数量 是否正常
        for (GoodsCartDto goodsCartDto : goodsCartInfoList) {
            if (goodsCartDto.getState() == 2) {
                return R.success("创建订单失败！商品【" + goodsCartDto.getName() + "】已下架!", false);
            }

            for (int i = 0; i < cartGoodsList.size(); i++) {
                //id相等
                if (cartGoodsList.get(i).getGoodsId().equals(goodsCartDto.getId())) {
                    //判断是否超出单次购买上限
                    if (cartGoodsList.get(i).getGoodsCount() > goodsCartDto.getMaxNum()) {
                        return R.success("创建订单失败！商品【" + goodsCartDto.getName() + "】超出单次购买最大数量!", false);
                    }
                    //判断库存是否充足
                    if (goodsAmountList.get(i) == null ||
                            Integer.parseInt((String) goodsAmountList.get(i)) < cartGoodsList.get(i).getGoodsCount()) {
                        return R.success("创建订单失败！商品【" + goodsCartDto.getName() + "】库存不足!", false);
                    }
                    // 对总金额进行计算
                    price = price.add(goodsCartDto.getPrice().multiply(new BigDecimal(cartGoodsList.get(i).getGoodsCount())));
                    //查找到相等后 无需再继续判断
                    break;
                }
            }
        }


        //封装keysId集合 和 扣减的库存数组
        List<String> deductAmountIdList = new ArrayList<>(cartGoodsList.size());
        Object[] numberArr = new Object[cartGoodsList.size()];
        for (int i = 0; i < cartGoodsList.size(); i++) {
            deductAmountIdList.add(i, String.valueOf(cartGoodsList.get(i).getGoodsId()));
            numberArr[i] = String.valueOf(cartGoodsList.get(i).getGoodsCount());
        }

        //lua脚本执行库存扣减
        Long result = stringRedisTemplate.execute(
                DEDUCT_GOODS_AMOUNT_SCRIPT,
                deductAmountIdList,
                numberArr);

        if (result == 0L) {
            //扣减失败
            return R.success("业务高峰期，您的购物车中存在商品库存不足!", false);

        }

        //库存减少完毕 进行创建订单
        //创建订单号
        long orderId = orderIdUtil.nextPayOrderId();
        //调用支付服务获取预支付信息
        HashMap<String, Object> prepayOrderMap = new HashMap<>();
        prepayOrderMap.put("orderId", orderId);
        prepayOrderMap.put("amount", price);
        prepayOrderMap.put("userId", userId);
        Map<String, Object> map;
        try {
            map = orderClient.createPrePayOrder(prepayOrderMap);
        } catch (Exception e) {
            e.printStackTrace();
            map = null;
        }

        if (map == null) {
            //支付订单创建失败 回滚数据
            rollbackGoodsAmount(cartGoodsList);
            return R.success("创建订单失败！", false);
        }

        String wxPrepayOrderId = String.valueOf(map.get("package")).split("=")[1];

        //订单异步入库、订单过期
        rabbitMqService.asynOrderEnduringAndOrderExpire
                (userId, address, cartGoodsList, goodsCartInfoList, price, orderId, wxPrepayOrderId, map,1);//订单类型为常规

        //异步线程清空用户购物车中选中的商品
        clearCart(userId);
        //返回结果
        return R.success(String.valueOf(orderId), map);
    }

    /**
     * @author xzh
     * @time 2022/12/27 20:20
     * 用户生成订单后 清空购物车所选择的商品
     */
    @Async("clearCartExecutors")
    public void clearCart(String userId) {
        this.remove(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId).eq(Cart::getIsChecked, 1));//TODO 常量
    }


    /**
     * @author xzh
     * @time 2023/2/21 0:12
     * 管道进行批量处理
     */
    private void rollbackGoodsAmount(List<Cart> deductAmountSuccessList) {
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Cart deductAmountSuccessGoods : deductAmountSuccessList) {
                connection.hashCommands().hIncrBy("goods:amount".getBytes(),
                        deductAmountSuccessGoods.getGoodsId().toString().getBytes(),
                        deductAmountSuccessGoods.getGoodsCount());
            }
            return null;
        });
    }

    @Override
    public R goodsSeckill(String userId, Integer seckillGoodsId, String address) {
        //从Redis中获取商品
        List<Object> redisResult = stringRedisTemplate.boundHashOps("goods:seckill:hash:" + seckillGoodsId)
                .multiGet(Arrays.asList("endTime", "beginTime", "name", "number", "promotionPrice", "smallLogo"));
        //判断商品是否存在
        if (redisResult == null || redisResult.get(0) == null) return R.success("商品不存在！", false);
        //判断商品秒杀是否结束
        if (DateStrUtil.DateStrToDate(redisResult.get(0).toString()).before(new Date())) return R.success("商品秒杀活动已经结束！", false);
        //判断商品秒杀是否开始
        if (DateStrUtil.DateStrToDate(redisResult.get(1).toString()).after(new Date())) return R.success("商品秒杀活动还未开始！", false);

        //判断商品秒杀库存是否充足
        if (Integer.parseInt((String) redisResult.get(3)) <= 0) return R.success("商品库存不足！", false);

        //Lua脚本判断秒杀
        Long result = stringRedisTemplate.execute(
                SECKILL_GOODS_AMOUNT_SCRIPT,
                Collections.singletonList(String.valueOf(seckillGoodsId)),
                String.valueOf(seckillGoodsId),
                userId);
        if (result == null || result == 0) return R.success("商品库存不足！", false);
        if (result == -1) return R.success("秒杀商品只能购买一次！", false);


        BigDecimal price = new BigDecimal((String) redisResult.get(4));

        //商品库存扣减成功 创建订单
        long orderId = orderIdUtil.nextPayOrderId();
        //调用支付服务获取预支付信息
        HashMap<String, Object> prepayOrderMap = new HashMap<>();
        prepayOrderMap.put("orderId", orderId);
        prepayOrderMap.put("amount", price);
        prepayOrderMap.put("userId", userId);
        Map<String, Object> map;
        try {
            map = orderClient.createPrePayOrder(prepayOrderMap);
        } catch (Exception e) {
            e.printStackTrace();
            map = null;
        }
        if (map == null) {
            //支付订单创建失败 回滚数据
            stringRedisTemplate.opsForHash()
                    .increment("goods:seckill:hash:" + seckillGoodsId, "number", 1);
            //删除已经抢购的记录
            stringRedisTemplate.opsForSet()
                    .remove("goods:seckill:success:userIdSet:"+seckillGoodsId,userId);
            return R.success("创建订单失败！请稍候重试！", false);
        }

        String wxPrepayOrderId = String.valueOf(map.get("package")).split("=")[1];

        //商品数量
        Cart cart = new Cart();
        cart.setGoodsId(seckillGoodsId);
        cart.setGoodsCount(1);
        //商品详情
        GoodsCartDto goodsCartDto = new GoodsCartDto();
        goodsCartDto.setId(seckillGoodsId);
        goodsCartDto.setPrice(price);
        goodsCartDto.setName(String.valueOf(redisResult.get(2)));
        goodsCartDto.setSmallLogo(String.valueOf(redisResult.get(5)));

        //订单异步入库、订单过期
        rabbitMqService.asynOrderEnduringAndOrderExpire
                (userId, address,
                        Collections.singletonList(cart),
                        Collections.singletonList(goodsCartDto),
                        price, orderId, wxPrepayOrderId, map,2);//订单类型 为秒杀

        //返回结果
        return R.success(String.valueOf(orderId), map);
    }
}
