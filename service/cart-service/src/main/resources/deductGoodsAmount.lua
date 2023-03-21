
-- 商品数量哈希集合key
local hashKey = KEYS[1];

-- 商品id
local goodsId = ARGV[1];

-- 购买数量
local amount = tonumber(ARGV[2]);

-- 判断是否存在
if(redis.call('HEXISTS',hashKey,goodsId) == 0) then
    return 0
end

-- 判断库存是否充足
if(tonumber(redis.call('hget',hashKey,goodsId)) < amount ) then
    return 0
end

-- 进行库存扣减
redis.call('HINCRBY',hashKey,goodsId,(0-amount))

return 1