package com.gdou.goods.config;

import org.redisson.Redisson;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xzh
 * @time 2022/12/23 14:50
 * Redisson配置类
 */
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient(){
        //配置
        Config config = new Config();
        config.useSingleServer().setAddress("");
        //创建Redisson对象
        return Redisson.create(config);
    }


    @Bean
    public RBloomFilter<Integer> seckillGoodsBloomFilter(){
        RBloomFilter<Integer> seckillGoodsBloomFilter = redissonClient().getBloomFilter("SeckillGoodsBloomFilter");
        if (!seckillGoodsBloomFilter.isExists()){
            //不存在进行初始化
            seckillGoodsBloomFilter.tryInit(2000,0.01);
        }
        return seckillGoodsBloomFilter;
    }


}
