
package com.fishedee.batch_call;

import java.util.function.Function;

public class ResultMatchByKey<ResultType,KeyType>{

    private Function<ResultType,KeyType> matcher;

    public ResultMatchByKey(Function<ResultType,KeyType> matcher){
        this.matcher = matcher;
    }

    public Function<ResultType,KeyType> getMatcher(){
        return this.matcher;
    }
}

