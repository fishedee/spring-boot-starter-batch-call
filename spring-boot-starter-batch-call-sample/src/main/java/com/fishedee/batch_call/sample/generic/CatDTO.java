package com.fishedee.batch_call.sample.generic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CatDTO {

    private Integer id;

    private String family;

    public void setCat(Cat cat){
        this.family = cat.family;
    }
}
