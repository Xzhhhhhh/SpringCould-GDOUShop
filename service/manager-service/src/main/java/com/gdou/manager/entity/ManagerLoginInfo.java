package com.gdou.manager.entity;



import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@TableName("tb_manager_login_info")
@NoArgsConstructor
@AllArgsConstructor
public class ManagerLoginInfo {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户登录ip地址
     */
    private String ipAddr;
    /**
     * 用户最后一次登陆时间
     */
    private Date LastLoginTime;

    /**
     * ip归属地
     */
    private String address;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 浏览器环境
     */
    private String browser;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}
