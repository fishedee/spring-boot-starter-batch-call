package com.fishedee.batch_call.sample.generic;

import java.util.List;

public class DataGenericDTO2<T> {

    private List<T> name;

    public void setName(List<T> data){
        this.name = data;
    }
}
