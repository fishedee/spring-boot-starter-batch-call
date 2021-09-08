package com.fishedee.batch_call.sample.generic;

import com.fishedee.batch_call.BatchCall;
import com.fishedee.batch_call.ResultMatch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SheepDTO extends DataGenericDTO2<String>{

    @BatchCall(
            task="getSheep",
            invokeTarget = SheepDao.class,
            invokeMethod = "getBatch",
            callbackMethod = "setName"
    )
    private Integer id;
}
