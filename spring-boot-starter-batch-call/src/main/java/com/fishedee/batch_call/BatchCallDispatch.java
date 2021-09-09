package com.fishedee.batch_call;

import java.util.List;
import java.util.function.BiFunction;

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
        this.config.setDipatchFuncArguListType(false);
        this.config.setDispatchFunc((Object a,Object b)->{
            dispatchFunc.apply((KeyObjectType)a,(KeyType)b);
            return null;
        });
        return new BatchCallRun(this.config);
    }

    public BatchCallRun dispatchList(BiFunctionVoid<KeyObjectType, List<KeyType>> dispatchFunc){
        this.config.setHasDispatchFunc(true);
        this.config.setDipatchFuncArguListType(true);
        this.config.setDispatchFunc((Object a,Object b)->{
            dispatchFunc.apply((KeyObjectType)a,(List<KeyType>)b);
            return null;
        });
        return new BatchCallRun(this.config);
    }

    public BatchCallRun noDispatch(){
        this.config.setHasDispatchFunc(false);
        return new BatchCallRun(this.config);
    }
}
