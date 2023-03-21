package com.gdou.user.filter;

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
import java.util.ArrayList;
import java.util.List;

import static com.gdou.constants.RedisConstants.USER_LOGIN_TOKEN_KEY;


@Component
public class ValidTokenGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //忽略的访问路径
        final List<String> ignoreURLs = new ArrayList<>();
        ignoreURLs.add("/goods/seckill");
        ignoreURLs.add("/goods/seckill/detail");
        ignoreURLs.add("/goods/swiperList");
        ignoreURLs.add("/goods/homeCategorize/list");
        ignoreURLs.add("/goods/floor/list");
        ignoreURLs.add("/goods/categorize");
        ignoreURLs.add("/goods/search");
        ignoreURLs.add("/goods/qsearch");
        ignoreURLs.add("/goods/detail");
        ignoreURLs.add("/order/callback");//放行微信支付成功的回调路径
        ignoreURLs.add("/order/refundCallback");//放行微信支付退款成功的回调路径
        ignoreURLs.add("/user/wxLogin");//放行微信登录路径
        // 符合放行接口 就放行
        if (ignoreURLs.contains(exchange.getRequest().getURI().getPath())) {
            return chain.filter(exchange);
        }

        //不是那两个则利用token进行身份校验
        HttpHeaders headers = exchange.getRequest().getHeaders();

        if (headers == null || headers.isEmpty()) {
            // 返回错误提示
            return wrapErrorResponse(exchange, chain);
        }

        String token = headers.getFirst("authorization");

        if (StringUtils.isEmpty(token)) {
            // 返回错误提示
            return wrapErrorResponse(exchange, chain);
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
            return wrapErrorResponse(exchange, chain);
        }


        //获取redis中所存的token 如果不存在 则说明过期    如果存在 但是不相等说明登录已经在别处登录 比对是否相等
        String redisToken = redisTemplate.opsForValue().get(USER_LOGIN_TOKEN_KEY+id);
        if (StrUtil.isBlank(redisToken)||!token.equals(redisToken)){
            return wrapErrorResponse(exchange, chain);
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    Mono<Void> wrapErrorResponse(ServerWebExchange exchange, GatewayFilterChain chain) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode resultNode = mapper.valueToTree(R.error("请登录后再进行访问",403));
        byte[] bytes = resultNode.toString().getBytes(StandardCharsets.UTF_8);
        DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap(bytes);
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        return exchange.getResponse().writeWith(Flux.just(dataBuffer));
    }

}