package com.fishedee.batch_call;

public class CallResultSizeNotEqualException extends BatchCallException{

    private Class targetClass;

    private int inputSize;

    private int outputSize;

    public CallResultSizeNotEqualException(Class clazz, int inputSize, int outputSize){
        super(clazz.getName()+" Call Result size "+outputSize+" is not equal to task size "+inputSize);
        this.targetClass = clazz;
        this.inputSize = inputSize;
        this.outputSize = outputSize;
    }

    public Class getTargetClass(){
        return this.targetClass;
    }

    public int getInputSize(){
        return this.inputSize;
    }

    public int getOutputSize(){
        return this.outputSize;
    }
}
