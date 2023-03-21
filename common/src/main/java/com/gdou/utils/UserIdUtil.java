package com.gdou.utils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author xzh
 * @time 2022/12/21 20:24
 * 通过用户请求获取 用户id
 */
public class UserIdUtil {

    public static String getUserId(HttpServletRequest request){
        //获取请求头中的token
        String token = request.getHeader("authorization");

        //解析用户id
        return JwtUtil.parseJWT(token).getSubject();
    }

}
