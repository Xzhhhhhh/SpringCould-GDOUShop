-- 商品秒杀lua脚本


-- Hash key
local hashKey = "goods:seckill:hash:"..KEYS[1];

-- 商品id
local seckillGoodsId = ARGV[1];

-- 用户id
local userId = ARGV[2];

-- SetKey
local setKey = "goods:seckill:success:userIdSet:"..seckillGoodsId;

--判断用户是否抢购过
if(redis.call("SISMEMBER",setKey,userId)==1) then
    return -1;
end

-- 判断商品库存是否充足
if (tonumber(redis.call('HGET', hashKey, "number")) > 0) then
    --执行库存扣减
    redis.call('HINCRBY', hashKey, "number",-1);
    -- 用户id加入抢购成功的 set集合中
    redis.call('SADD',setKey,userId);
    return 1;
else
    -- 商品库存不充足
    return 0;
end