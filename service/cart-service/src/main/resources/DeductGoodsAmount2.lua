-- 对多个商品 进行扣减库存 如果存在不足 则回滚
local hashKey = "goods:amount";

-- 商品id集合
local goodsIdList = KEYS;

---- 商品数量集合
local goodsNumberList = ARGV;

-- 回滚的table
local rollBackTable = {};


local function rollBackAmount(table,redisKey)
    for key, value in pairs(table) do
        redis.call('HINCRBY',redisKey,key,value);
    end
end


for i,id in ipairs(goodsIdList) do
    for j,number in ipairs(goodsNumberList) do
        if(i == j) then
            --索引相等 判断库存是否充足 充足扣减库存
            if(tonumber(redis.call('HGET',hashKey,id)) >= tonumber(number)) then
                redis.call('HINCRBY',hashKey,id,(0-tonumber(number)));
                -- 加入回滚的Map中
                rollBackTable[id] = tonumber(number);
            else
            --不充足 进行回滚
                rollBackAmount(rollBackTable,hashKey);
                return 0;
            end
        end

    end
end

return 1;