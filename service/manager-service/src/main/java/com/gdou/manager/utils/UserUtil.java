package com.gdou.manager.utils;


import cn.hutool.core.util.ObjectUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserUtil {
    public static String getLoginUserId(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(ObjectUtil.isNull(principal)||principal.equals("anonymousUser")){
            return null;
        }
        return principal.toString();
    }
}
