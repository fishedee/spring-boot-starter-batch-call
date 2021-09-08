package com.fishedee.batch_call.sample.generic;

import com.fishedee.batch_call.BatchCall;

import java.util.List;
import java.util.stream.Collectors;

public class DataGenericDTO<T extends DataGenericDTO.NameGetter> {

    public interface NameGetter{
        String getName();
    }

    private List<String> name;

    public void setName(List<T> data){
        this.name = data.stream().map((single)->{
            return single.getName();
        }).collect(Collectors.toList());
    }
}
