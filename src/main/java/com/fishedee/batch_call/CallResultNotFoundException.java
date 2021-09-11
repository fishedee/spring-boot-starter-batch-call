package com.fishedee.batch_call;

public class CallResultNotFoundException extends BatchCallException{
    private Class targetClass;

    private Object key;

    public CallResultNotFoundException(Class clazz,Object key){
        super("在"+clazz.getName()+"中找不到ID为"+key+"结果");
        this.targetClass = clazz;
        this.key = key;
    }

    public Class getTargetClass(){
        return this.targetClass;
    }

    public Object getKey(){
        return this.key;
    }
}
