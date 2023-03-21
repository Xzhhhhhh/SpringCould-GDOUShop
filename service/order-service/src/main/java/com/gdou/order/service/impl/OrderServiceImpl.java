package com.gdou.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdou.R;
import com.gdou.feign.client.GoodsClient;
import com.gdou.feign.client.UserClient;
import com.gdou.feign.entity.User;
import com.gdou.feign.entity.dto.GoodsCartDto;
import com.gdou.feign.entity.dto.GoodsOrderDto;
import com.gdou.order.entity.*;
import com.gdou.order.mapper.OrderMapper;
import com.gdou.order.mapper.OrderPaymentMapper;
import com.gdou.order.service.OrderDetailService;
import com.gdou.order.service.OrderService;
import com.gdou.order.util.OrderIdUtil;
import com.gdou.order.util.WxPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserClient userClient;

    @Autowired
    private OrderIdUtil orderIdUtil;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private OrderPaymentMapper orderPaymentMapper;

    @Autowired
    private GoodsClient goodsClient;


    /**
     * @author xzh
     * @time 2023/1/7 19:03
     * 生成预支付订单
     */
    @Override
    public Map<String, Object> createPrePayOrder(String userId, String orderId, BigDecimal amount) {

        //查询用户信息
        User userInfo = userClient.getUserById(Long.valueOf(userId));

        //订单存在 则创建预支付订单
        //调用微信创建订单
        return WxPayUtil.createOrder("用户【" + userInfo.getNickname() + "】的商品",//商品名称
                orderId,//订单号
                userInfo.getOpenId(),//用户openId
                amount.multiply(new BigDecimal(100)).intValue());
    }


    /**
     * @author xzh
     * @time 2023/1/7 20:15
     * 用户完成支付后 查看支付结果
     */
    @Override
    public R checkPayResult(String userId, String orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            return R.success("订单不存在！", false);
        }
        //校验结果
        if (order.getState() == 0) {//TODO 未支付常量
            return R.success("订单未支付!", false);
        } else if (order.getState() == 2) {//todo 取消常量
            return R.success("订单已取消!支付金额会稍后退还回您微信账户!", false);
        }
        //支付成功
        return R.success("支付成功！");
    }

    /**
     * @author xzh
     * @time 2023/1/7 16:44
     * 微信提示订单支付成功进行处理
     */
    @Override
    public void paySuccessCallBack(String orderId, String wxOrderId, String amount) {
        //订单成功回调锁
        RLock callbackLock = redissonClient.getLock("lock:orderCallback:" + orderId);
        if (callbackLock.tryLock()) {
            try {
                //订单成功回调 与 订单手动取消 或者 自动取消的锁
                RLock orderStateLock = redissonClient.getLock("lock:orderState:" + orderId);
                if (orderStateLock.tryLock()) {
                    try {
                        // 获取锁成功
                        //进一步判断是否已经 取消或过期
                        Order orderAgain = this.getOne(new LambdaQueryWrapper<Order>()
                                .eq(Order::getId, orderId));
                        //如果是 不存在 或者 未支付
                        if (orderAgain == null || orderAgain.getState() == 0) {
                            Order payOrder = new Order();
                            payOrder.setId(Long.valueOf(orderId));
                            payOrder.setWxOrderId(wxOrderId);
                            payOrder.setState(1);//设置状态为已支付
                            payOrder.setPayTime(new Date());
                            //设置订单为已支付
                            this.saveOrUpdate(payOrder, new LambdaQueryWrapper<Order>()
                                    .eq(Order::getId, Long.valueOf(orderId)).eq(Order::getState, 0));
                        } else if (orderAgain.getState() == 2) {
                            //订单已经取消
                            //判断是否退款
                            if (orderAgain.getIsRefund() == 0) {
                                //未退款则进行退款
                                this.update().eq("id", orderId)
                                        .set("wx_order_id", wxOrderId)
                                        .set("is_refund", 1).update();
                                //异步任务进行退款
                                orderRefund(orderId, amount);
                            }
                        }
                        //如果状态为1 这说明已经支付 不用理会
                    } finally {
                        //释放锁
                        orderStateLock.unlock();
                    }
                } else {
                    //没抢到锁 说明 已被取消订单抢去锁 进行退款
                    //进行退款 判断是否已经退款
                    //查询订单信息
                    Order order = this.getOne(new LambdaQueryWrapper<Order>()
                            .eq(Order::getId, orderId));
                    if (order.getIsRefund() == 0) {
                        //未退款则进行退款
                        this.update().eq("id", orderId)
                                .set("wx_order_id", wxOrderId)
                                .set("is_refund", 1).update();
                        //异步任务进行退款
                        orderRefund(orderId, amount);
                    }
                }
            } finally {
                callbackLock.unlock();
            }
        }
    }

    /**
     * @author xzh
     * @time 2023/1/26 18:41
     * 订单异步退款
     */
    @Async("handleOrderRefundExecutors")
    public void orderRefund(String orderId, String amount) {
        String refundReason = "订单已取消！";
        //查询订单详情集合
        List<OrderDetail> orderDetailList = orderDetailService.list(new LambdaQueryWrapper<OrderDetail>()
                .eq(OrderDetail::getOrderId, orderId));

        //进行遍历退款
        orderDetailList.forEach(orderDetail -> {
            //生成退款id
            long refundId = orderIdUtil.nextOrderRefundId();

            Map<String, Object> refundResult = WxPayUtil.orderRefund(orderId, String.valueOf(refundId), refundReason, amount, orderDetail);

            String status = refundResult.get("status").toString();
            String wxRefundId = refundResult.get("wx_refund_id").toString();
            if (status.equals("SUCCESS")) {
                orderDetail.setState(2);//todo 已退款
                orderDetail.setRefundSuccessTime((Date) refundResult.get("success_time"));
            } else if (status.equals("PROCESSING")) {
                orderDetail.setState(1);// 处理中
            }
            orderDetail.setWxRefundId(wxRefundId);
            orderDetail.setRefundTime((Date) refundResult.get("refund_time"));
            orderDetail.setRefundId(refundId);
        });
        //进行批量修改
        orderDetailService.updateBatchById(orderDetailList);
    }


    /**
     * @author xzh
     * @time 2023/1/25 22:45
     * 进行订单入库
     */
    @Override
    @Transactional
    public boolean saveOrderInfo(MqOrder mqOrder) {

        long orderId = mqOrder.getOrderId();
        //先判断订单是否存在并且未支付 如果是则不需要再进行操作
        Order isExist = this.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId).select(Order::getId, Order::getUserId));

        //已经入库过 则直接返回成功
        if (isExist != null && isExist.getUserId() != null) return true;

        //未入库 mysql库存扣减
        if (isExist == null || isExist.getUserId() == null)
            goodsNumberDeductionDurable(mqOrder.getCartGoodsList(), mqOrder.getOrderType());

        //未入库过
        //订单入库
        Order order = new Order();
        order.setId(orderId);
        order.setAmount(mqOrder.getAmount());
        order.setUserId(mqOrder.getUserId());
        order.setWxPrepayOrderId(mqOrder.getWxPrepayOrderId());
        order.setCreateTime(mqOrder.getCreateTime());
        order.setOrderType(mqOrder.getOrderType());
        //如果不存在 设置为未退款 否则保持原样
        if (isExist == null) {
            order.setIsRefund(0);//todo 是否退款
        }
        this.saveOrUpdate(order);

        //订单详情入库
        //==============创建订单详情========
        //创建订单详情列表
        List<OrderDetail> orderDetailList = new ArrayList<>();

        List<GoodsCartDto> goodsCartInfoList = mqOrder.getGoodsCartInfoList();
        List<Cart> cartGoodsList = mqOrder.getCartGoodsList();
        //封装订单详情
        for (GoodsCartDto goodsCartDto : goodsCartInfoList) {
            for (Cart cart : cartGoodsList) {
                if (cart.getGoodsId().equals(goodsCartDto.getId())) {
                    //生成详细订单id
                    long orderDetailId = orderIdUtil.nextGoodsOrderId(cart.getGoodsCount());

                    //订单详情对象
                    OrderDetail orderDetail = new OrderDetail();
                    //订单详情id
                    orderDetail.setId(orderDetailId);
                    //订单收获地址
                    orderDetail.setAddress(mqOrder.getAddress());
                    //订单所属支付订单
                    orderDetail.setOrderId(orderId);
                    //订单的商品Id
                    orderDetail.setGoodsId(goodsCartDto.getId());
                    //订单金额
                    orderDetail.setTotalPrice(goodsCartDto.getPrice()
                            .multiply(new BigDecimal(cart.getGoodsCount())));
                    //订单名称
                    orderDetail.setGoodsName(goodsCartDto.getName());
                    //订单详情商品数量
                    orderDetail.setGoodsCount(cart.getGoodsCount());
                    //订单详情小图片
                    orderDetail.setSmallLogo(goodsCartDto.getSmallLogo());
                    if (isExist == null) {
                        //如果不存在设置为支付状态正常，否则保持原样
                        //支付状态 0 正常 TODO
                        orderDetail.setState(0);//常量
                    }

                    //所属用户Id
                    orderDetail.setUserId(mqOrder.getUserId());
                    //加入集合
                    orderDetailList.add(orderDetail);
                }
            }
        }
        //批量保存 订单详情
        orderDetailService.saveOrUpdateBatch(orderDetailList);

        //支付信息 入库
        ObjectMapper mapper = new ObjectMapper();
        OrderPayment orderPayment = new OrderPayment();
        orderPayment.setOrderId(orderId);
        try {
            orderPayment.setOrderPaymentDetail(mapper.writeValueAsString(mqOrder.getPayOrderMap()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        //支付信息入库
        orderPaymentMapper.insert(orderPayment);
        return true;
    }

    /**
     * @author xzh
     * @time 2023/2/19 14:46
     * mysql库存扣减
     */
    private void goodsNumberDeductionDurable(List<Cart> cartGoodsList, Integer orderType) {
        //转换
        List<GoodsOrderDto> goodsOrderDtoList = new ArrayList<>();
        cartGoodsList.forEach(cartGoods -> {
            GoodsOrderDto goodsOrderDto = new GoodsOrderDto();
            goodsOrderDto.setId(cartGoods.getGoodsId());
            goodsOrderDto.setGoodsCount(cartGoods.getGoodsCount());
            goodsOrderDtoList.add(goodsOrderDto);
        });
        if (orderType == 1) {
            //普通订单
            goodsClient.goodsNumberDeductionDurable(goodsOrderDtoList);
        } else if (orderType == 2) {
            //秒杀商品
            goodsClient.seckillGoodsNumberDeductionDurable(goodsOrderDtoList);
        }

    }

    /**
     * @author xzh
     * @time 2023/1/25 23:29
     * 超过30分钟订单未支付 过期处理
     */
    @Override
    public void orderExpire(MqOrder mqOrder) {

        //加锁  订单取消 和 订单过期 不可以同时进行
        //如果 订单不存在 并且订单未过期、未取消 则进行过期
        RLock lock = redissonClient.getLock("lock:orderState:" + mqOrder.getOrderId());
        //如果获取锁成功
        if (lock.tryLock()) {
            try {
                //进一步判断是否已经 取消或过期
                Order orderAgain = this.getOne(new LambdaQueryWrapper<Order>()
                        .eq(Order::getId, mqOrder.getOrderId()));
                //如果还是 不存在 或者 未支付 则进行订单过期
                if (orderAgain == null || orderAgain.getState() == 0) {
                    //执行订单过期
                    log.info("订单【{}】超时未支付，自动取消！" + new Date(), mqOrder.getOrderId());

                    //执行订单取消
                    Order cancelOrder = new Order();
                    cancelOrder.setId(mqOrder.getOrderId());
                    cancelOrder.setOrderType(mqOrder.getOrderType());
                    cancelOrder.setState(2);//设置状态为已取消

                    //取消订单
                    this.saveOrUpdate(cancelOrder, new LambdaQueryWrapper<Order>()
                            .eq(Order::getId, mqOrder.getOrderId()).eq(Order::getState, 0));

                    //订单取消完毕 执行库存回滚
                    rollbackGoodsAmount(mqOrder.getCartGoodsList(),mqOrder.getOrderType());
                }
            } finally {
                //释放锁
                lock.unlock();
            }
        }
        //如果 订单存在 且已支付或已取消 或未抢到锁(说明在取消的过程中) 则忽略此次过期

    }

    /**
     * @author xzh
     * @time 2023/1/26 12:25
     * 库存回滚
     */
    private void rollbackGoodsAmount(List<Cart> deductAmountSuccessList,Integer orderType) {
        //转换
        List<GoodsOrderDto> goodsOrderDtoList = new ArrayList<>();
        deductAmountSuccessList.forEach(cartGoods -> {
            GoodsOrderDto goodsOrderDto = new GoodsOrderDto();
            goodsOrderDto.setId(cartGoods.getGoodsId());
            goodsOrderDto.setGoodsCount(cartGoods.getGoodsCount());
            goodsOrderDtoList.add(goodsOrderDto);
        });
        //普通商品库存回滚
        if (orderType == 1){
            //mysql数据回滚
            goodsClient.goodsNumberRollBackDurable(goodsOrderDtoList);
            //redis数据回滚
            for (Cart deductAmountSuccessGoods : deductAmountSuccessList) {
                stringRedisTemplate.opsForHash()
                        .increment("goods:amount",
                                deductAmountSuccessGoods.getGoodsId().toString(),
                                deductAmountSuccessGoods.getGoodsCount());
            }
            //秒杀商品库存回滚
        }else if(orderType == 2){
            //mysql数据回滚
            goodsClient.seckillGoodsNumberRollBackDurable(goodsOrderDtoList);
            //redis数据回滚
            for (Cart deductAmountSuccessGoods : deductAmountSuccessList) {
                stringRedisTemplate.opsForHash()
                        .increment("goods:seckill:hash:"+deductAmountSuccessGoods.getGoodsId(),
                               "number",
                                deductAmountSuccessGoods.getGoodsCount());
            }
        }

    }

    /**
     * @author xzh
     * @time 2023/1/27 10:28
     * 修改订单退款状态
     */
    @Override
    public void updateOrderDetailRefundState(String refund_status, Date successTime, String refund_id, String wx_refund_id) {
        UpdateWrapper<OrderDetail> updateWrapper = new UpdateWrapper<>();
        if (refund_status.equals("SUCCESS")) {
            updateWrapper.set("refund_success_time", successTime);
            updateWrapper.set("state", 2);
        } else if (refund_status.equals("ABNORMAL")) {
            //退款异常
            updateWrapper.set("state", 3);
        }
        updateWrapper.eq("wx_refund_id", wx_refund_id)
                .eq("refund_id", refund_id)
                .eq("state", 1);//状态必须是退款中
        orderDetailService.update(updateWrapper);
    }
}
