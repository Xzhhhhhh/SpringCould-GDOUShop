package com.gdou.manager.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Manager {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String nickname;

    private String username;

    private String password;

    private String avatar;

    private Integer isDisabled;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}
