package com.gdou.manager.security.handler;

import com.alibaba.fastjson.JSON;
import com.gdou.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class LoginFailHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        exception.printStackTrace();
        ServletOutputStream outputStream = response.getOutputStream();
        log.info("登陆失败！ 原因：【{}】",exception.getMessage());
        R r= R.error(exception.getMessage(),44444);//标记登录失败
        outputStream.write(JSON.toJSONBytes(r));
        outputStream.flush();
        outputStream.close();;
    }
}
