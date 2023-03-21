package com.gdou.manager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gdou.manager.entity.Manager;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ManagerService extends IService<Manager> {
    void recordLoginInfo(HttpServletRequest request,String username);

    List<String> getManagerAuthorityList(String userId);
}
