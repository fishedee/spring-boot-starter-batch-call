package com.fishedee.batch_call.sample.generic;

import lombok.Data;

@Data
public class Dog implements DataGenericDTO.NameGetter{
    Integer id;

    String name;
}
