package com.fishedee.batch_call.sample.generic;

import com.fishedee.batch_call.BatchCall;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DataGenericDTO2<T> {

    private List<T> name;

    public void setName(List<T> data){
        this.name = data;
    }
}
