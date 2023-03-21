package com.gdou.manager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gdou.manager.entity.Manager;
import com.gdou.manager.entity.ManagerLoginInfo;
import com.gdou.manager.mapper.ManagerMapper;
import com.gdou.manager.service.ManagerLoginInfoService;
import com.gdou.manager.service.ManagerService;
import com.gdou.utils.AddressUtils;
import com.gdou.utils.IpUtils;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Service
public class ManagerServiceImpl extends ServiceImpl<ManagerMapper, Manager> implements ManagerService {

    @Autowired
    private ManagerLoginInfoService infoService;



    /**
     * @author xzh
     * @time 2022/12/18 12:40
     * 异步执行 记录登录成功信息
     */
    @Override
    @Async("ManagerLoginInfoExecutor")//自定义线程池执行
    public void recordLoginInfo(HttpServletRequest request, String id) {
        //通过存入登录信息
        //保存登录信息
        //获取用户访问ip地址
        String ipAddr = IpUtils.getIpAddr(request);
        //解析IP地址
        String address = AddressUtils.getAddressByIP(ipAddr);
        //获取操作系统类型
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        String os = userAgent.getOperatingSystem().getName();
        //获取浏览器类型
        String browser = userAgent.getBrowser().getName();
        ManagerLoginInfo loginInfo = ManagerLoginInfo.builder()
                .id(Long.valueOf(id))
                .ipAddr(ipAddr)
                .address(address)
                .browser(browser)
                .os(os)
                .LastLoginTime(new Date())
                .build();

        infoService.saveOrUpdate(loginInfo);
    }

    /**
     * @author xzh
     * @time 2022/12/18 22:07
     * 获取用户的权限
     */
    @Override
    public List<String> getManagerAuthorityList(String userId) {
        return baseMapper.selectPermsByUserId(userId);
    }
}
