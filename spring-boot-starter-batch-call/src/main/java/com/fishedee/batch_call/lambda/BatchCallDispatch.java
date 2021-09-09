package com.fishedee.batch_call.lambda;

import java.util.function.BiFunction;
import java.util.function.Function;

public class BatchCallDispatch<KeyObjectType,KeyType> {
    private Config config;

    public BatchCallDispatch(Config config){
        this.config = config;
    }

    public<ReturnType> BatchCallRun dispatch(BiFunction<KeyObjectType, KeyType,ReturnType> dispatchFunc){
        this.config.setHasDispatchFunc(true);
        this.config.setDispatchFunc(dispatchFunc);
        return new BatchCallRun(this.config);
    }

    public BatchCallRun dispatch(BiFunctionVoid<KeyObjectType, KeyType> dispatchFunc){
        this.config.setHasDispatchFunc(true);
        this.config.setDispatchFunc((KeyObjectType a,KeyType b)->{
            dispatchFunc.apply(a,b);
            return null;
        });
        return new BatchCallRun(this.config);
    }

    public BatchCallRun noDispatch(){
        this.config.setHasDispatchFunc(false);
        return new BatchCallRun(this.config);
    }
}
