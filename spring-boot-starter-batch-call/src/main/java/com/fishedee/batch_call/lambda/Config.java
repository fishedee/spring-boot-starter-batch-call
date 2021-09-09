package com.fishedee.batch_call.lambda;

import lombok.Data;

import java.util.function.BiFunction;
import java.util.function.Function;

@Data
public class Config {

    //收集的对象
    private Class<?> keyObjectType;

    //收集的对象key获取方法
    private Function<?,?> collectFunc;

    //收集的对象回调方法
    private BiFunction<?,?,?> dispatchFunc;

    //是否有对象回调方法
    private boolean hasDispatchFunc;

    //批量调用的对象
    private Object callTarget;

    //批量调用的方法
    private BiFunction<?,?,?> callFunc;

    //批量调用的结果匹配
    private ResultMatch matcher;

    //KEY匹配方式的获取KEY方式
    private Function<?,?> callResultMatchByKey;

    //KEY匹配方式时的默认值
    private Object callResultMatchByKeyDefault;

    //KEY匹配方式是否有默认值
    private boolean hasCallResultMatchByKeyDefault;

}
