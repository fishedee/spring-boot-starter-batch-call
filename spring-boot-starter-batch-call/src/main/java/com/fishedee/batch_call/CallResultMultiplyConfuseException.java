package com.fishedee.batch_call;

public class CallResultMultiplyConfuseException extends BatchCallException{
    private String debugName;

    private Object key;

    public CallResultMultiplyConfuseException(String debugName,Object key){
        super(debugName+"ID"+key+"对应的结果太多");
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
