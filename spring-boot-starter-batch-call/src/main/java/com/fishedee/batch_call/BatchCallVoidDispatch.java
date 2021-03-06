package com.fishedee.batch_call;

import java.util.function.Function;

public class BatchCallVoidDispatch<KeyObjectType> {
    private Config config;

    public BatchCallVoidDispatch(Config config){
        this.config = config;
    }

    public<ReturnType> BatchCallRun dispatch(Function<KeyObjectType,ReturnType> dispatchFunc){
        this.config.setHasDispatchFunc(true);
        this.config.setDipatchFuncArguListType(false);
        this.config.setDispatchFunc((Object a,Object b)->{
            return dispatchFunc.apply((KeyObjectType)a);
        });
        return new BatchCallRun(this.config);
    }

    public BatchCallRun dispatch(FunctionVoid<KeyObjectType> dispatchFunc){
        this.config.setHasDispatchFunc(true);
        this.config.setDipatchFuncArguListType(false);
        this.config.setDispatchFunc((Object a,Object b)->{
            dispatchFunc.apply((KeyObjectType)a);
            return null;
        });
        return new BatchCallRun(this.config);
    }

    public BatchCallRun noDispatch(){
        this.config.setHasDispatchFunc(false);
        return new BatchCallRun(this.config);
    }
}
