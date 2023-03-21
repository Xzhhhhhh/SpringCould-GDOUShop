package com.gdou.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;
/**
 * @author xzh
 * @time 2022/9/11 22:46
 *
 *      自动填充配置类
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
        this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
       // this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
        this.setFieldValByName("updateTime",new Date(),metaObject);
    }
}