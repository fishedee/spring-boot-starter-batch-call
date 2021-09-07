package com.fishedee.batch_call;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BatchCall {
    //task name，argument for BatchCallTask.invokeTask()
    String task();

    //invoke target
    Class invokeTarget();

    //invoke target method name
    String invokeMethod();

    //callback method name
    String callbackMethod();

    //name for debug
    String name() default "";

    //max size for each batch
    int batchSize() default 0;

    //match type
    ResultMatch resultMatch() default ResultMatch.SEQUENCE;

    //allow not found?
    boolean allowResultNotFound() default false;

    //match key
    String resultMatchKey() default "";

    //cacheEnabled
    boolean cacheEnabled() default false;
}
