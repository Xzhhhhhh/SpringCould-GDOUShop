package com.gdou.user.controller;



import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gdou.R;
import com.gdou.feign.entity.User;
import com.gdou.user.service.UserService;
import com.gdou.utils.JwtUtil;
import com.gdou.utils.UserIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;

import static com.gdou.constants.RedisConstants.USER_LOGIN_TOKEN_KEY;


@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping
    public User getOne(@RequestParam("id")Long id){
        return userService.getById(id);
    }

    @GetMapping("/logout")
    public R logout(HttpServletRequest request) {
        String userId = UserIdUtil.getUserId(request);
        redisTemplate.delete(USER_LOGIN_TOKEN_KEY + userId);

        return R.success("退出登录成功！");

    }

    /**
     * @author xzh
     * @time 2023/1/4 19:31
     * 微信登录接口
     */
    @PostMapping("/wxLogin")
    public R wxLogin(HttpServletRequest request, HttpServletResponse response){

        User user;

        try {
            //获取微信传来的参数
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line = null;
            while ((line = reader.readLine())!=null){
                builder.append(line);
            }

            JSONObject jsonObject = JSON.parseObject(builder.toString());

            //执行微信登录
            user =  userService.wxLogin(jsonObject);
        }catch (Exception e){
            e.printStackTrace();
            return R.success("登陆失败！",false);
        }

        if(user.getIsDisabled()==1){
           return R.success("该用户已被禁用！请联系管理员！",false);
        }

        //生成token 存入Redis
        String token = createTokenAndSaveInRedis(user.getId().toString());

        //存入响应头中
        response.setHeader("authorization",token);

        //登陆成功
        //登录成功信息返回
       return R.success("登陆成功", true);//标记登录成功
    }

    public String createTokenAndSaveInRedis(String id){
        //生成token
        String token = JwtUtil.createNeverExpiresJWT(id);
        //存入redis永不过期Token
        redisTemplate.opsForValue().set(USER_LOGIN_TOKEN_KEY+id,token);
        return token;
    }



}
