package com.fishedee.batch_call;

public class BatchCallException extends RuntimeException{
    public BatchCallException(String message){
        super(message);
    }

    public BatchCallException(String message,Throwable e){
        super(message,e);
    }
}
