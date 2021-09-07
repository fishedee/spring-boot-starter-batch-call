package com.fishedee.batch_call;

public class InvokeReflectMethodException extends BatchCallException{
    public InvokeReflectMethodException(Exception e){
        super("invoke reflect method fail ",e);
    }
}
