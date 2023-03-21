package com.gdou.user.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gdou.feign.entity.User;
import com.gdou.user.entity.UserDto;
import com.gdou.user.mapper.UserMapper;
import com.gdou.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.gdou.constants.WxConstant.APPID;
import static com.gdou.constants.WxConstant.SECRET;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    /**
     * @author xzh
     * @time 2022/12/21 15:13
     * 执行微信登录流程
     */
    @Override
    public User wxLogin(JSONObject jsonObject) {
        //获取登录所需信息
        String code =   String.valueOf(jsonObject.get("code"));
        //用户身份信息
        Object rawData = jsonObject.get("rawData");
        String jsonString = JSONObject.toJSONString(rawData);
        UserDto userDto = JSONObject.parseObject(JSON.parse(jsonString).toString(), UserDto.class);
        String avatar = userDto.getAvatarUrl();
        String nickname = userDto.getNickName();

        //调用wx认证
        JSONObject sessionKeyOrOpenId = getSessionKeyOrOpenId(code);

        //获取openId
        String openid = sessionKeyOrOpenId.getString("openid");

        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getOpenId, openid));

        //如果用户没有注册过
        if (user == null) {
            // 用户信息入库 进行注册
            user = new User();
            user.setOpenId(openid);
            user.setAvatar(avatar);
            user.setNickname(nickname);
            user.setIsDisabled(0);//todo 未禁用 常量
            this.save(user);
        }else {
            user.setOpenId(openid);
            user.setAvatar(avatar);
            this.updateById(user);
        }

        return user;
    }

    public static JSONObject getSessionKeyOrOpenId(String code) {
        String requestUrl = "https://api.weixin.qq.com/sns/jscode2session";
        Map<String, Object> requestUrlParam = new HashMap<>();
        // https://mp.weixin.qq.com/wxopen/devprofile?action=get_profile&token=164113089&lang=zh_CN
        //小程序appId
        requestUrlParam.put("appid",APPID);
        //小程序secret
        requestUrlParam.put("secret", SECRET);
        //小程序端返回的code
        requestUrlParam.put("js_code", code);
        //默认参数
        requestUrlParam.put("grant_type", "authorization_code");

        //发送post请求读取调用微信接口获取openid用户唯一标识
        JSONObject jsonObject = JSON.parseObject(HttpUtil.post(requestUrl,requestUrlParam));
        return jsonObject;
    }

}
