# SpringCould-GDOUShop
该项目是前端优购小程序项目的后台接口，使用微服务框架进行搭建，具有较为完善的订单模块功能。该程序完全由本人亲自设计、编写和发布，虽然不一定保证系统的设计是最优解，但是花费了挺多时间去设计了。尽量做到在高并发场景下的系统吞吐量减少和保障数据安全问题。


项目分为七个模块
- cart-service 购物车模块
- goods-service 商品模块
- order-service 订单模块
- user-service 用户模块
- manager-service 管理员模块
- manager-gateway 管理员相关服务网关
- user-gateway 用户相关模块网关

## cart-service
购物车模块主要功能有：

1、处理用户购物车相关数据请求的模块，利用mysql对与用户购物车数据的存储。在此模块中着重设计了商品加入购物车后，商品的状态发生变化后导致的购物车中的勾选状态也需要发生变化，避免在结算时发送异常。同时采用乐观锁避免并发的商品添加或减少请求过于频繁导致的购物车中数据错乱。

2、处理订单结算的请求，在购物车模块中首先对该用户的购物车商品状态进行判断，然后利用lua脚本对用户购物车所选中的商品库存进行批量扣减，当库存不足时回滚库存，利用lua脚本的原子性可以保证此过程是线程安全的。然后生成订单id(利用了redis自增保证id唯一且自增),调用order-service获取微信支付的订单，返回给用户调起微信支付。(同时在生成完成订单后，利用RabbitMq对订单信息进行异步持久化操作，还有mysql持久化的库存扣减，此事件将由购物车模块发布，由订单模块监听处理。以及订单过期消息，也将一同发布。)此模块功能较为复杂，源码中均有体现。

## goods-service
无论是管理员模块还是用户模块，商品数据都需要通过feign调用goods模块来获取商品数据(商品库存除外，商品库存存于redis，用于提高频繁操作情况下的执行效率)。所以goods-service主要是对于外部提供商品信息的接口，维护数据库中持久化的库存，从而保障商品库存数据的最终一致性和数据安全性。

## order-service
订单模块是着重设计的模块之一，在订单模块中主要功能有：
1、处理购物车模块所调用的微信支付订单申请。

2、监听消息队列对异步入库的订单，此处包括订单异步入库。

3、处理微信支付成功的回调或对已过期订单进行数据回滚等。

## user-service 和 manager-service
利用SpringSecurity对用户登录请求进行处理。管理员模块暂未完善，在学校没什么时间。o(╥﹏╥)o

## user-gateway 和 manager-gateway
依据不同请求路径进行转发到对应的服务器中，后期可以拓展限流等功能。

## 总结
项目目前没有特别完善，里面有些常量还没替换好，还在用字面量。但是着重优化的下单逻辑我认为应该是生产可用的(在小程序上测试过基本没问题)，秒杀商品库存一样安安全全~ 支付和退款啥的都能到账。贴点截图吧

项目首页

![Snipaste_2023-03-21_20-55-09](https://user-images.githubusercontent.com/91795546/226612234-d4d7b24d-88f5-4e5a-972b-14b497fae3c6.png)

 秒杀列表
 
![Snipaste_2023-03-21_20-57-24](https://user-images.githubusercontent.com/91795546/226612888-ea9034a8-ee1d-4433-bf8b-9d6b2664c8eb.png)

商品分类

![Snipaste_2023-03-21_20-56-25](https://user-images.githubusercontent.com/91795546/226612805-b872a312-ae83-4156-8804-3852f0b3a498.png)

用户购物车

![Snipaste_2023-03-21_21-00-14](https://user-images.githubusercontent.com/91795546/226613450-d9f11d35-4d83-4b8c-bb31-84db68ce0b8d.png)


微信商户收款成功

![Snipaste_2023-03-21_20-57-08](https://user-images.githubusercontent.com/91795546/226612985-28c40c16-cfbe-453a-abc8-706f3877ebad.png)


微信商户退款成功

![Snipaste_2023-03-21_20-56-50](https://user-images.githubusercontent.com/91795546/226613007-f2b6380f-1036-48eb-84d0-427fd6dc1780.png)
