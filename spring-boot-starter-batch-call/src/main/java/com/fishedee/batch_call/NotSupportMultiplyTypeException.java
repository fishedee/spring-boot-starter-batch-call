package com.fishedee.batch_call;

public class NotSupportMultiplyTypeException extends BatchCallException{
    public NotSupportMultiplyTypeException(){
        super("not support multiply type with @BatchCall annotation");
    }
}
