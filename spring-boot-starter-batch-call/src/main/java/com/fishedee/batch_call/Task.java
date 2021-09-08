package com.fishedee.batch_call;

import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

@Data
public class Task {
    @Data
    public static class Config{
        private BatchCall batchCall;

        private Class clazz;

        //字段最终也是转换为方法获取的，所以field也是方法
        private Method getKeyMethod;

        private Method callbackMethod;

        private boolean callbackMethodArgumentIsListType;

        private Class invokeTarget;

        private Method invokeMethod;

        private boolean callbackMethodArgumentIsEmpty;

        private Method resultKeyMethod;
    }

    private Object instance;

    private Object key;

    private Config config;
}
