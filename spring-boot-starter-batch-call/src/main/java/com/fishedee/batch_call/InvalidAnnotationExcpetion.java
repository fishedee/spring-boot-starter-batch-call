package com.fishedee.batch_call;

public class InvalidAnnotationExcpetion extends BatchCallException{
    public InvalidAnnotationExcpetion(String message){
        super(message);
    }

    public InvalidAnnotationExcpetion(String message,Exception e){
        super(message,e);
    }
}
