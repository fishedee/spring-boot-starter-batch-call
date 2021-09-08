package com.fishedee.batch_call;

public class InvalidAnnotationExcpetion extends BatchCallException{
    public InvalidAnnotationExcpetion(String prefix,String message){
        super(prefix+message);
    }

    public InvalidAnnotationExcpetion(String prefix,String message,Exception e){
        super(prefix+message,e);
    }
}
