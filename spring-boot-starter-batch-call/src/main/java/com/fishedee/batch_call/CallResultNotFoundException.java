package com.fishedee.batch_call;

public class CallResultNotFoundException extends BatchCallException{
    private String debugName;

    private Object key;

    public CallResultNotFoundException(String debugName,Object key){
        super("找不到"+debugName+"ID为"+key+"结果");
        this.debugName = debugName;
        this.key = key;
    }

    public String getDebugName(){
        return this.debugName;
    }

    public Object getKey(){
        return this.key;
    }
}
