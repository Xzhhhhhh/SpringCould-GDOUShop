package com.gdou.manager.filter;


import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gdou.R;
import com.gdou.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static com.gdou.constants.RedisConstants.MANAGER_LOGIN_TOKEN_KEY;


@Component
public class ValidTokenGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 如果是 管理员 开头直接放行 交给管理员进行判断
        if (exchange.getRequest().getURI().getPath().startsWith("/manager")) {
            return chain.filter(exchange);
        }

        //不是那两个则利用token进行身份校验
        HttpHeaders headers = exchange.getRequest().getHeaders();

        if (headers == null || headers.isEmpty()) {
            // 返回错误提示
            return wrapErrorResponse(exchange, chain,"无权限访问");
        }

        String token = headers.getFirst("authorization");

        if (StringUtils.isEmpty(token)) {
            // 返回错误提示
            return wrapErrorResponse(exchange, chain,"无权限访问");
        }


        String username;
        Claims token_claims;
        /**
         * 解析token
         */
        try {
            token_claims = JwtUtil.parseJWT(token);
            username = token_claims.getSubject();
        } catch (JwtException errorException) {
            //如果token是错误的
            //拒绝放行 认证错误
            return wrapErrorResponse(exchange, chain,"无权限访问");
        }

        //获取redis中所存的token 如果不存在 则说明过期    如果存在 但是不相等说明登录已经在别处登录 比对是否相等
        String redisToken = redisTemplate.opsForValue().get(MANAGER_LOGIN_TOKEN_KEY+username);
        if (StrUtil.isBlank(redisToken)||!token.equals(redisToken)){
            return wrapErrorResponse(exchange, chain,"认证过期请重新登陆！");
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    Mono<Void> wrapErrorResponse(ServerWebExchange exchange, GatewayFilterChain chain,String message) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode resultNode = mapper.valueToTree(R.error(message,403));
        byte[] bytes = resultNode.toString().getBytes(StandardCharsets.UTF_8);
        DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap(bytes);
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        return exchange.getResponse().writeWith(Flux.just(dataBuffer));
    }

}