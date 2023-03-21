package com.gdou.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * 获取地址工具类
 *
 * @Author zhangxin
 **/
public class AddressUtils {

    /**
     * 根据IP地址获取地理位置
     */
    public static String getAddressByIP(String ip) {
        try {
            String url = "https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php?resource_id=6006&format=json&query=" + ip;
            HttpResponse res = HttpRequest.get(url).execute();
            Object data = JSONUtil.parseObj(res.body()).getObj("data");
            JSONObject json = new JSONObject(JSONUtil.parseArray(data).get(0));
            Object location = json.get("location");
            return location.toString().split(" ")[0];
        }catch (Exception e){
            return "";
        }
    }
}







