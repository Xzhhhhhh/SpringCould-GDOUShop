package com.gdou.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gdou.R;
import com.gdou.order.entity.MqOrder;
import com.gdou.order.entity.Order;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

public interface OrderService extends IService<Order> {

    void paySuccessCallBack(String orderId, String wxOrderId,String amount);

    Map<String, Object> createPrePayOrder(String userId, String orderId, BigDecimal amount);

    R checkPayResult(String userId, String orderId);

    boolean saveOrderInfo(MqOrder mqOrder);

    void orderExpire(MqOrder mqOrder);

    void updateOrderDetailRefundState(String refund_status, Date successTime, String refund_id, String wx_refund_id);
}
