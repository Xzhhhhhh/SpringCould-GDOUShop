package com.gdou.order.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdou.R;
import com.gdou.order.service.OrderService;
import com.gdou.order.util.WxDateUtil;
import com.gdou.order.util.WxPayUtil;
import com.gdou.utils.UserIdUtil;
import com.wechat.pay.contrib.apache.httpclient.notification.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * @author xzh
     * @time 2023/1/7 19:02
     * 创建微信预支付订单
     */
    @PostMapping("/prePayOrder")
    public Map<String, Object> createPrePayOrder(@RequestBody HashMap<String,Object> prepayOrderMap) {
        String orderId = String.valueOf(prepayOrderMap.get("orderId"));
        BigDecimal price =BigDecimal.valueOf((Double) prepayOrderMap.get("amount"));
        String userId = String.valueOf(prepayOrderMap.get("userId"));
        return orderService.createPrePayOrder(userId,orderId,price);
    }

    /**
     * @author xzh
     * @time 2022/12/27 17:34
     * 用户微信支付成功后 微信的支付通知接口
     */
    @PostMapping("/callback")
    public Map callback(HttpServletRequest request) {
        String timestamp = request.getHeader("Wechatpay-Timestamp");//时间戳
        String nonce = request.getHeader("Wechatpay-Nonce");//随机数
        String serial = request.getHeader("Wechatpay-Serial");//序列号
        String signature = request.getHeader("Wechatpay-Signature");//签名

        Map result = new HashMap();
        result.put("code", "FAIL");

        try {

            BufferedReader br = request.getReader();
            String str = null;
            StringBuilder builder = new StringBuilder();//请求主体
            while ((str = br.readLine()) != null) {
                builder.append(str);
            }

            //验证签名和解析主体
            Notification notification = WxPayUtil.signVerifyAndGetBody(serial,
                    nonce, timestamp, builder.toString(), signature);

            //如果解析主体为空则失败
            if (notification==null){
                return result;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode decryptData = objectMapper.readTree(notification.getDecryptData());

            //获取详情
            String orderId = String.valueOf(decryptData.get("out_trade_no")).replace("\"","");
            String wxOrderId = String.valueOf(decryptData.get("transaction_id")).replace("\"","");
            String tradeState = String.valueOf(decryptData.get("trade_state")).replace("\"","");
            String amount = String.valueOf(decryptData.get("amount").get("payer_total"));//付款金额

            if ("SUCCESS".equals(tradeState)){
                //微信返回是成功则进行处理
                //修改订单状态
                orderService.paySuccessCallBack(orderId, wxOrderId, amount);
            }
            //未出现任何错误 返回success
            result.put("code", "SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            //出现任何错误都提前返回 失败结果
            return result;
        }
        return result;
    }

    /**
     * @author xzh
     * @time 2023/1/7 20:13
     * 用户完成支付后 调用该接口检查支付结果
     */
    @PostMapping("/checkOrder")
    public R checkPayResult(HttpServletRequest request,@RequestBody HashMap<String,String> orderIdMap){
        String userId = UserIdUtil.getUserId(request);
        String orderId = orderIdMap.get("orderId");
        return orderService.checkPayResult(userId,orderId);
    }

    /**
     * @author xzh
     * @time 2023/1/27 10:13
     * 微信订单退款回调
     */
    @PostMapping("/refundCallback")
    public Map wxRefundCallback(HttpServletRequest request){
        String timestamp = request.getHeader("Wechatpay-Timestamp");//时间戳
        String nonce = request.getHeader("Wechatpay-Nonce");//随机数
        String serial = request.getHeader("Wechatpay-Serial");//序列号
        String signature = request.getHeader("Wechatpay-Signature");//签名

        Map result = new HashMap();
        result.put("code", "FAIL");

        try {
            BufferedReader br = request.getReader();
            String str = null;
            StringBuilder builder = new StringBuilder();//请求主体
            while ((str = br.readLine()) != null) {
                builder.append(str);
            }

            //验证签名和解析主体
            Notification notification = WxPayUtil.signVerifyAndGetBody(serial,
                    nonce, timestamp, builder.toString(), signature);

            //如果解析主体为空则失败
            if (notification == null) {
                return result;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode decryptData = objectMapper.readTree(notification.getDecryptData());

            String refund_status = decryptData.get("refund_status").textValue().replace("\"", "");
            Date successTime = null;
            if (refund_status.equals("SUCCESS")){
                successTime = WxDateUtil.wxDateStrToDate(decryptData.get("success_time").textValue().replace("\"", ""));
            }
            String refund_id = decryptData.get("out_refund_no").textValue().replace("\"", "");
            String wx_refund_id = decryptData.get("refund_id").textValue().replace("\"", "");

            //修改订单状态
            orderService.updateOrderDetailRefundState(refund_status,successTime,refund_id,wx_refund_id);

            //未出现任何错误 返回success
            result.put("code", "SUCCESS");
        }catch (Exception e){
            e.printStackTrace();
            return result;
        }
        return result;
    }


}
