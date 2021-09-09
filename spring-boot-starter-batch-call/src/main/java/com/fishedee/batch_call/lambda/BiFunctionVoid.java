package com.fishedee.batch_call.lambda;

@FunctionalInterface
public interface BiFunctionVoid<T, U> {
    void apply(T t, U u);
}