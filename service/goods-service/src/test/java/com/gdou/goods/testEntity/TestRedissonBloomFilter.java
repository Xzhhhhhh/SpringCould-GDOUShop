//package com.gdou.goods.testEntity;
//
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.redisson.api.RBloomFilter;
//import org.redisson.api.RedissonClient;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//@SpringBootTest
//@RunWith(SpringRunner.class)
//public class TestRedissonBloomFilter {
//
//    @Autowired
//    private RedissonClient redissonClient;
//    @Test
//    public void  test1(){
//        RBloomFilter<Integer> bloomFilter = redissonClient.getBloomFilter("test1");
//        bloomFilter.tryInit(2000,0.01);
//
//        System.out.println(bloomFilter.isExists());
//
//
//        RBloomFilter<Integer> bloomFilter2 = redissonClient.getBloomFilter("test2");
//        bloomFilter.tryInit(2000,0.01);
//        System.out.println(bloomFilter2.isExists());
////        for (int i = 11;i<1500 ;i++){
////            bloomFilter.add(i);
////        }
////
////        System.out.println(bloomFilter.contains(33));
////        System.out.println(bloomFilter.contains(414));
////        System.out.println(bloomFilter.contains(1));
//
//
//    }
//
//    @Autowired
//    private RBloomFilter<Integer> seckillGoodsBloomFilter;
//
//    @Test
//    public void  test2(){
//        System.out.println(seckillGoodsBloomFilter.isExists());
//    }
//}
