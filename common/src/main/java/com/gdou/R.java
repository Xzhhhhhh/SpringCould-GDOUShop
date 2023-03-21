package com.gdou;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class R {

    private String message;
    private Integer code;
    private Object data;
    private boolean flag;

    public static R success(String message,Object o){
        return new R(message,200,o,true);
    }

    public static R success(String message,Object o,boolean flag){
        return new R(message,200,o,flag);
    }

    public static R success(String message,Integer code){
        return new R(message,code,null,true);
    }

    public static R success(Object data){
        R r=new R();
        r.setCode(200);
        r.setMessage("成功");
        r.setData(data);
        r.setFlag(true);
        return r;
    }

    public static R success(String message){
        R r=new R();
        r.setCode(200);
        r.setMessage(message);
        r.setFlag(true);
        return r;
    }


    public static R success(String message,boolean flag){
        R r=new R();
        r.setCode(200);
        r.setMessage(message);
        r.setFlag(flag);
        return r;
    }

    public static R error(String message){
        R r=new R(message,400,null,true);
        return r;
    }

    public static R error(String message,Integer code){
        R r=new R(message,code,null,false);
        return r;
    }

}
