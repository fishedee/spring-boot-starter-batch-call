package com.fishedee.batch_call.sample.generic;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Data
public class DataGenericDTO2<T> {

    private List<T> name;

    public void setName(List<T> data){
        this.name = data;
    }
}
