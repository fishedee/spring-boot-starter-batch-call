package com.fishedee.batch_call;

@FunctionalInterface
public interface BiFunctionVoid<T, U> {
    void apply(T t, U u);
}