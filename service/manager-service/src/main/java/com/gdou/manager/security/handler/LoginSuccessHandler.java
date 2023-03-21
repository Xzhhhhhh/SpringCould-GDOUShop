package com.gdou.manager.security.handler;


import com.alibaba.fastjson.JSON;

import com.gdou.R;
import com.gdou.manager.service.ManagerService;
import com.gdou.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.gdou.constants.RedisConstants.*;

/**
 * @author xzh
 * @time 2022/12/2 16:09
 *
 */
@Slf4j
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Resource
    private ManagerService managerService;

    @Autowired
    private StringRedisTemplate redisTemplate;



    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String id = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        //登陆成功记录登录信息
        managerService.recordLoginInfo(request,id);

        //生成token 存入Redis
        String token = createTokenAndSaveInRedis(id);

        response.setContentType("application/json;charset=UTF-8");
        //存入响应头中
        response.setHeader("authorization",token);
        ServletOutputStream outputStream = response.getOutputStream();

        //登录成功信息返回
        R r = R.success("登陆成功", true);//标记登录成功
        outputStream.write(JSON.toJSONBytes(r));
        outputStream.flush();
        outputStream.close();

    }

    public String createTokenAndSaveInRedis(String id){
        //先 生成accessToken 2个小时过期一次
        String token = JwtUtil.createNeverExpiresJWT(id);
        //存入redis中用于过期Token
        redisTemplate.opsForValue().set(MANAGER_LOGIN_TOKEN_KEY+id,token,MANAGER_LOGIN_KEY_TTL, TimeUnit.SECONDS);
        return token;
    }
}
