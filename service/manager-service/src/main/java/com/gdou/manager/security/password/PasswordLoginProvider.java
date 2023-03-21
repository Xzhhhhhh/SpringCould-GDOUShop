package com.gdou.manager.security.password;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gdou.manager.entity.Manager;
import com.gdou.manager.service.ManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class PasswordLoginProvider implements AuthenticationProvider {

    @Resource
    private ManagerService managerService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PasswordLoginToken passwordLoginToken= (PasswordLoginToken) authentication;
        String username = passwordLoginToken.getId();
        String password = passwordLoginToken.getPassword();
        //检验是否为空
        if(StrUtil.isBlank(username)||StrUtil.isBlank(password)){
            throw new BadCredentialsException("用户名或密码为空");
        }
        //判断是否存在这个用户
        Manager manager = managerService.getOne(new LambdaQueryWrapper<Manager>().eq(Manager::getUsername, username));
        if(ObjectUtil.isNull(manager)){
            throw new BadCredentialsException("用户不存在");
        }
        //存在这个用户 判断密码是否正确
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean matches = encoder.matches(password, manager.getPassword());
        if (matches){
            //密码正确
            //再判断是否禁用
            if(manager.getIsDisabled()==1){
                throw new BadCredentialsException("该用户已被禁用！请联系管理员！");
            }
            //查询用户权限
            List<GrantedAuthority> authorities = Collections.emptyList();

            return new PasswordLoginToken(manager.getId().toString(),password,authorities);
        }else {
            throw new BadCredentialsException("用户名或密码错误");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PasswordLoginToken.class.isAssignableFrom(authentication);
    }
}