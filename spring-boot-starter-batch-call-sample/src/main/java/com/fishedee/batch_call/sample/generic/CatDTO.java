package com.fishedee.batch_call.sample.generic;

import com.fishedee.batch_call.BatchCall;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CatDTO {

    @BatchCall(
            task="getCat",
            invokeTarget = CatDao.class,
            invokeMethod = "getBatch",
            callbackMethod = "setCat"
    )
    private Integer id;

    private String family;

    public void setCat(Cat cat){
        this.family = cat.family;
    }
}
