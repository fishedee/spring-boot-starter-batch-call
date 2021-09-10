package com.fishedee.batch_call;

@FunctionalInterface
public interface FunctionVoid<T> {
    void apply(T a);
}