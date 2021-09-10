package com.fishedee.batch_call;

public class CallResultMultiplyConfuseException extends BatchCallException{
    private Class targetClass;

    private Object key;

    public CallResultMultiplyConfuseException(Class clazz,Object key){
        super("在"+clazz.getName()+"中找到ID"+key+"对应的结果太多");
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
