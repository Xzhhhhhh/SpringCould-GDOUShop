package com.gdou.feign.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;

@FeignClient("order-service")
public interface OrderClient {

    @PostMapping("/order/prePayOrder")
    Map<String, Object> createPrePayOrder(@RequestBody HashMap<String,Object> prepayOrderMap);
}
