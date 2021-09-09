package com.fishedee.batch_call.lambda;

@FunctionalInterface
public interface FunctionVoid<T> {
    T apply();
}