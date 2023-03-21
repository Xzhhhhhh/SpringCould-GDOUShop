package com.gdou.order.util;

import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gdou.order.entity.OrderDetail;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.notification.Notification;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationHandler;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationRequest;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.gdou.order.constant.PayConstant.*;

public class WxPayUtil {

    private static CloseableHttpClient httpClient;

    private static CertificatesManager certificatesManager;

    private static Verifier verifier;


    public static PrivateKey setup() throws Exception {
        PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(PRIVATE_KEY);
        // 获取证书管理器实例
        certificatesManager = CertificatesManager.getInstance();
        // 向证书管理器增加需要自动更新平台证书的商户信息
        certificatesManager.putMerchant(MERCHANT_ID, new WechatPay2Credentials(MERCHANT_ID,
                        new PrivateKeySigner(MERCHANT_SERIAL_NUMBER, merchantPrivateKey)),
                API_V3_KEY.getBytes(StandardCharsets.UTF_8));
        // 从证书管理器中获取verifier
        verifier = certificatesManager.getVerifier(MERCHANT_ID);
        httpClient = WechatPayHttpClientBuilder.create()
                .withMerchant(MERCHANT_ID, MERCHANT_SERIAL_NUMBER, merchantPrivateKey)
                .withValidator(new WechatPay2Validator(certificatesManager.getVerifier(MERCHANT_ID)))
                .build();
        return merchantPrivateKey;
    }


    public static HashMap<String, Object> createOrder(String goodsName, String orderId, String openId, Integer amount) {

        //最后的结果
        HashMap<String, Object> map = new HashMap<>();

        try {
            //初始化参数
            PrivateKey merchantPrivateKey = setup();
            HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi");
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Content-type", "application/json; charset=utf-8");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectMapper objectMapper = new ObjectMapper();

            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("mchid", MERCHANT_ID)//商户号
                    .put("appid", APP_ID)//小程序id
                    .put("description", goodsName)//商品描述
                    .put("notify_url", "") //支付成功回调地址
                    .put("out_trade_no", orderId); //我的唯一订单号
            rootNode.putObject("amount")//支付价格 分为单位
                    .put("total", amount);

            rootNode.putObject("payer") //支付者 appId下对应的用户
                    .put("openid", openId);

            objectMapper.writeValue(bos, rootNode);

            httpPost.setEntity(new StringEntity(bos.toString("UTF-8"), "UTF-8"));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String bodyAsString = EntityUtils.toString(response.getEntity());

//=======================创建签名返回给前端=========================================================

            //时间戳
            String timeStamp = System.currentTimeMillis() + "";
            map.put("timeStamp", timeStamp);
            //随机字符串
            String nonceStr = RandomUtil.randomString(32);
            map.put("nonceStr", nonceStr);
            //预支付会话id
            JsonNode node = objectMapper.readTree(bodyAsString);
            String Package = "prepay_id=" + node.get("prepay_id").textValue().replace("\"", "");
            map.put("package", Package);
            //签名方式
            map.put("signType", "RSA");

            //应用id
            String str = APP_ID + "\n" +
                    //时间戳
                    timeStamp + "\n" +
                    //随机字符串
                    nonceStr + "\n" +
                    //预支付会话id
                    Package + "\n";

            //生成签名
            String sign = sign(str.getBytes(), merchantPrivateKey);
            //签名
            map.put("paySign", sign);

        } catch (Exception e) {
            //出现任何问题都返回空 标识创建失败
            return null;
        } finally {
            //无论什么情况都需要关闭流
            try {
                httpClient.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return map;
    }

    private static String sign(byte[] message, PrivateKey privateKey) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(privateKey);
        sign.update(message);
        return Base64.getEncoder().encodeToString(sign.sign());
    }

    /**
     * @author xzh
     * @time 2022/12/27 17:45
     * 验证签名和解析主体
     */
    public static Notification signVerifyAndGetBody(String serial, String nonce, String timestamp, String body, String signature) {
        String result = null;
        try {
            setup();
            // 构建request，传入必要参数
            NotificationRequest request = new NotificationRequest.Builder().withSerialNumber(serial)
                    .withNonce(nonce)
                    .withTimestamp(timestamp)
                    .withSignature(signature)
                    .withBody(body)
                    .build();
            NotificationHandler handler = new NotificationHandler(verifier, API_V3_KEY.getBytes(StandardCharsets.UTF_8));
            // 验签和解析请求体
            Notification notification = handler.parse(request);

            return notification;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @author xzh
     * @time 2023/1/26 17:36
     * 订单退款
     */
    public static Map<String, Object> orderRefund(String orderId, String refundId, String refundReason,
                                                  String amount, OrderDetail orderDetail) {
        try {
            setup();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/v3/refund/domestic/refunds");
        httpPost.addHeader("Accept", "application/json");
        httpPost.addHeader("Content-type", "application/json; charset=utf-8");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = objectMapper.createObjectNode();


        rootNode.put("out_trade_no", orderId);
        rootNode.put("out_refund_no", refundId);
        rootNode.put("reason", refundReason);
        rootNode.putObject("amount")//支付价格 分为单位
                .put("refund", orderDetail.getTotalPrice()
                        .multiply(new BigDecimal(100)).intValue())
                .put("total", Integer.valueOf(amount))
                .put("currency", "CNY");

        HashMap<String, Object> map = new HashMap<>();
        String body;
        try {
            objectMapper.writeValue(bos, rootNode);
            httpPost.setEntity(new StringEntity(bos.toString("UTF-8"), "UTF-8"));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            body = EntityUtils.toString(response.getEntity());

            //获取消息
            JsonNode jsonNode = objectMapper.readTree(body);
            String wx_refund_id = jsonNode.get("refund_id").textValue().replace("\"", "");
            Date refund_time = WxDateUtil.wxDateStrToDate(
                    jsonNode.get("create_time").textValue().replace("\"", ""));
            String status = jsonNode.get("status").textValue().replace("\"", "");

            if (status.equals("SUCCESS")) {
                Date success_time = WxDateUtil.wxDateStrToDate(jsonNode.get("success_time").textValue().replace("\"", ""));
                map.put("success_time",success_time);
            }
            map.put("wx_refund_id", wx_refund_id);
            map.put("refund_time", refund_time);
            map.put("status", status);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return map;

    }

}
