package com.fishedee.batch_call.lambda;

import lombok.Data;

@Data
public class Task {
    private Object instance;

    private Object key;
}
