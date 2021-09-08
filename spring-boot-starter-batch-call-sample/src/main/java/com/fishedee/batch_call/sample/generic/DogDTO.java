package com.fishedee.batch_call.sample.generic;

import com.fishedee.batch_call.BatchCall;
import com.fishedee.batch_call.ResultMatch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DogDTO extends DataGenericDTO<Dog>{

    @BatchCall(
            task="getDog",
            invokeTarget = DogDao.class,
            invokeMethod = "getBatch",
            callbackMethod = "setName",
            resultMatch = ResultMatch.KEY,
            resultMatchKey = "id"
    )
    private Integer id;
}
