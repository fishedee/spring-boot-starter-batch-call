package com.fishedee.batch_call;

@FunctionalInterface
public interface FunctionVoid<T> {
    T apply();
}