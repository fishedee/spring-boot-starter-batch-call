package com.fishedee.batch_call.sample;

import java.util.List;

@FunctionalInterface
public interface MyFunc<T,U> {
    List<T> getBatch(List<U> data);
}
