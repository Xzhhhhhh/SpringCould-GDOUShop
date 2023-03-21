package com.gdou.manager.security.handler;


import com.alibaba.fastjson.JSON;
import com.gdou.R;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;


import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 *
 * 自定义认证失败类
 */

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        /**
         *  40001标识认证失败
         *  40002 标识需要刷新token
         */
        R r;
        if(authException instanceof CredentialsExpiredException){
            //认证需要刷新40002
            r= R.success(authException.getMessage(),40002);
        }else{
            r= R.success(authException.getMessage(),40001);
        }

        response.setContentType("application/json;charset=UTF-8");
        ServletOutputStream outputStream = response.getOutputStream();

        outputStream.write(JSON.toJSONBytes(r));
        outputStream.flush();
        outputStream.close();;
    }
}
