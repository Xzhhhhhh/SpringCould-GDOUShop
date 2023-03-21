package com.gdou.manager.security.jwt;

import cn.hutool.core.util.StrUtil;
import com.gdou.manager.security.handler.JwtAuthenticationEntryPoint;
import com.gdou.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;



import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.gdou.constants.RedisConstants.MANAGER_LOGIN_TOKEN_KEY;


@Slf4j
public class JWTAuthenticationFilter extends BasicAuthenticationFilter {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;;

    @Autowired
    private StringRedisTemplate redisTemplate;


    //    提供一个无参构造方法来应用父类的构造方法 避免覆盖父类
    public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        String token = request.getHeader("authorization");

        //是空获取是要登陆就放行
        if (StrUtil.isEmptyOrUndefined(token)){
            chain.doFilter(request, response);
            return;
        }

        String id;
        Claims token_claims;
        /**
         * 解析token
         */
        try {
            token_claims = JwtUtil.parseJWT(token);
            id = token_claims.getSubject();
        } catch (JwtException errorException) {
            //如果token是错误的
            //拒绝放行 认证错误
            SecurityContextHolder.clearContext();
            this.jwtAuthenticationEntryPoint.commence(request, response,new BadCredentialsException("认证失败！请重新登陆！"));
            return;
        }

        //获取redis中所存的access_token 如果不存在 则说明过期    如果存在 但是不相等说明登录已经在别处登录
        String redisToken = redisTemplate.opsForValue().get(MANAGER_LOGIN_TOKEN_KEY+id);
        if (StrUtil.isBlank(redisToken)){
            //登录信息过期
            SecurityContextHolder.clearContext();
            this.jwtAuthenticationEntryPoint.commence(request, response,new BadCredentialsException("登录超时！请重新登录！"));
            return;
        }
        /**
         * 比对是否相等 如果完全相等则通过   如果 一个为空 一个相等则提示刷新  否则提示失败
         */
        if (!token.equals(redisToken)){
            //不相等
            SecurityContextHolder.clearContext();
            this.jwtAuthenticationEntryPoint.commence(request, response,new BadCredentialsException("认证信息失效！请重新登录！"));
            return;
        }
        //相等
        //认证成功！
        //存入权限信息
        List<GrantedAuthority> userAuthority = Collections.emptyList();
        JwtAuthenticationToken jwtAuthenticationToken =
                new JwtAuthenticationToken(userAuthority,id);
        //自定义一个Details
        jwtAuthenticationToken.setDetails(
                ((AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails>)
                        context -> new WebAuthenticationDetails(context)).buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationToken);

        chain.doFilter(request, response);
    }
}

