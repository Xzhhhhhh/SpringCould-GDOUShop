package com.gdou.manager.security.password;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdou.manager.entity.Manager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Slf4j
public class PasswordLoginFilter extends AbstractAuthenticationProcessingFilter {

    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = new AntPathRequestMatcher("/manager/login","POST");

    public PasswordLoginFilter(AuthenticationManager authenticationManager) {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER);
        super.setAuthenticationManager(authenticationManager);
    }
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
        //获取用户名和密码
        ObjectMapper mapper = new ObjectMapper();
        Manager manager ;
        try {
             manager = mapper.readValue(request.getInputStream(), Manager.class);
        }catch (Exception e){
            log.warn("【管理员模块】用户登录参数异常！");
            return null;
        }
        //生成待验证token
        PasswordLoginToken token=new PasswordLoginToken(manager.getUsername(),manager.getPassword());
        setDetails(request,token);
        //进行验证
        return this.getAuthenticationManager().authenticate(token);
    }

    protected void setDetails(HttpServletRequest request, PasswordLoginToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }
}
