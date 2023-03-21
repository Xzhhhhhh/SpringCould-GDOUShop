package com.gdou.manager.security.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.gdou.constants.RedisConstants.MANAGER_LOGIN_TOKEN_KEY;


/**
 * @author xzh
 * @time 2022/9/15 10:18
 *  自定义的退出登录流程
 */
@Slf4j
@Component
public class LogoutHandler implements org.springframework.security.web.authentication.logout.LogoutHandler {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        //获取用户id
        String username = (String) authentication.getPrincipal();
        //删除redis中的数据
        redisTemplate.delete(MANAGER_LOGIN_TOKEN_KEY+username);
        log.info("用户id【{}】,退出登录!",username);
    }
}

