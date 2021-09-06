package com.fishedee.batch_call;

import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

@Data
public class Task {
    public static class Config{
        BatchCall batchCall;

        Class clazz;

        //字段最终也是转换为方法获取的，所以field也是方法
        Method getKeyMethod;

        Method callbackMethod;

        Class invokeTarget;

        Method invokeMethod;
    }
    Object instance;

    Object key;

    Config config;

    public String getConfigIdentityName(){
        return this.config.clazz.getName()+"_"+this.config.getKeyMethod.getName();
    }
}
