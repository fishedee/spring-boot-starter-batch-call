package com.fishedee.batch_call.lambda;

import java.util.function.Function;

public class BatchCallVoidDispatch<KeyObjectType> {
    private Config config;

    public BatchCallVoidDispatch(Config config){
        this.config = config;
    }

    public<ReturnType> BatchCallRun dispatch(Function<KeyObjectType,ReturnType> dispatchFunc){
        this.config.setHasDispatchFunc(true);
        this.config.setDispatchFunc((KeyObjectType a,Object b)->{
            dispatchFunc.apply(a);
            return null;
        });
        return new BatchCallRun(this.config);
    }


    public BatchCallRun noDispatch(){
        this.config.setHasDispatchFunc(false);
        return new BatchCallRun(this.config);
    }
}
