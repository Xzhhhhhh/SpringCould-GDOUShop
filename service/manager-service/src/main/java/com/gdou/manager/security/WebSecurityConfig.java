package com.gdou.manager.security;

import com.gdou.manager.security.handler.LoginFailHandler;
import com.gdou.manager.security.handler.LoginSuccessHandler;
import com.gdou.manager.security.handler.LogoutHandler;
import com.gdou.manager.security.handler.LogoutSuccessHandler;
import com.gdou.manager.security.jwt.JWTAuthenticationFilter;
import com.gdou.manager.security.jwt.JwtAuthenticationProvider;
import com.gdou.manager.security.password.PasswordLoginFilter;
import com.gdou.manager.security.password.PasswordLoginProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


import org.springframework.security.web.authentication.logout.LogoutFilter;



@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private PasswordLoginProvider passwordLoginProvider;
    @Autowired
    private LogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    private LoginSuccessHandler loginSuccessHandler;
    @Autowired
    private LoginFailHandler loginFailHandler;

    @Autowired
    private JwtAuthenticationProvider jwtAuthenticationProvider;

    @Autowired
    private LogoutHandler logoutHandler;

    /**
     * @author xzh
     * @time 2022/12/2 14:09
     * 获取AuthenticationManager
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public JWTAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        return new JWTAuthenticationFilter(authenticationManagerBean());
    }

    @Bean
    public PasswordLoginFilter passwordLoginFilter() throws Exception {
        PasswordLoginFilter passwordLoginFilter = new PasswordLoginFilter(authenticationManager());
        passwordLoginFilter.setAuthenticationSuccessHandler(loginSuccessHandler);
        passwordLoginFilter.setAuthenticationFailureHandler(loginFailHandler);
        return passwordLoginFilter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(passwordLoginProvider);
        auth.authenticationProvider(jwtAuthenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .logout()
                .addLogoutHandler(logoutHandler)
                .logoutUrl("/logout")
                .logoutSuccessHandler(logoutSuccessHandler)
                .and()
                .authorizeRequests()
                .antMatchers(URL_WHITELIST).permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter(), LogoutFilter.class)
                .addFilter(jwtAuthenticationFilter());
    }

    /**
     * 不进行安全校验的白名单
     */
    public static final String [] URL_WHITELIST={
            "/manager/login",//查看登录成功信息
            "/manager/logout",//退出登录
            "/manager",
            "/websocket",
    };


}
