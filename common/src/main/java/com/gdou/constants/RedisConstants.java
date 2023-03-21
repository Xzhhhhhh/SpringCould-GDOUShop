package com.gdou.constants;

public class RedisConstants {

    //管理员登录token前缀
    public final static String MANAGER_LOGIN_TOKEN_KEY = "manager:login:token:";

    //管理员登录token过期时间
    public final static Long MANAGER_LOGIN_KEY_TTL = 2 * 24 * 60 * 60L;

    //用户登录token前缀
    public final static String USER_LOGIN_TOKEN_KEY="user:login:token:";

    //用户登录accessToken 过期时间 8小时
    public final static Long USER_LOGIN_TOKEN_KEY_TTL=  7 * 24 * 60 * 60L;

}
