package com.gdou.user.service;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gdou.feign.entity.User;

import java.util.Map;


public interface UserService extends IService<User> {
    User wxLogin(JSONObject jsonObject);

}
