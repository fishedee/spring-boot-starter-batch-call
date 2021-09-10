package com.fishedee.batch_call;

import sun.reflect.generics.tree.ReturnType;

import java.util.List;
import java.util.function.BiFunction;

public class BatchCallDispatch<KeyObjectType,CallResultType,KeyType> {
    private Config config;

    public BatchCallDispatch(Config config){
        this.config = config;
    }

    public<ReturnType> BatchCallRun<KeyType,ReturnType> dispatch(BiFunction<KeyObjectType, CallResultType,ReturnType> dispatchFunc){
        this.config.setHasDispatchFunc(true);
        this.config.setDispatchFunc(dispatchFunc);
        return new BatchCallRun<KeyType,ReturnType>(this.config);
    }

    public BatchCallRun<KeyType,Void> dispatch(BiFunctionVoid<KeyObjectType, CallResultType> dispatchFunc){
        this.config.setHasDispatchFunc(true);
        this.config.setDipatchFuncArguListType(false);
        this.config.setDispatchFunc((Object a,Object b)->{
            dispatchFunc.apply((KeyObjectType)a,(CallResultType)b);
            return null;
        });
        return new BatchCallRun<KeyType,Void>(this.config);
    }

    public<ReturnType> BatchCallRun<KeyType,ReturnType> groupAndThenDispatch(BiFunction<KeyObjectType, List<CallResultType>,ReturnType> dispatchFunc){
        this.config.setHasDispatchFunc(true);
        this.config.setDipatchFuncArguListType(true);
        this.config.setDispatchFunc(dispatchFunc);
        return new BatchCallRun<KeyType,ReturnType>(this.config);
    }

    public BatchCallRun<KeyType,Void> groupAndThenDispatch(BiFunctionVoid<KeyObjectType, List<CallResultType>> dispatchFunc){
        this.config.setHasDispatchFunc(true);
        this.config.setDipatchFuncArguListType(true);
        this.config.setDispatchFunc((Object a,Object b)->{
            dispatchFunc.apply((KeyObjectType)a,(List<CallResultType>)b);
            return null;
        });
        return new BatchCallRun<KeyType, Void>(this.config);
    }

    public BatchCallRun<KeyType,Void> noDispatch(){
        this.config.setHasDispatchFunc(false);
        return new BatchCallRun<KeyType,Void>(this.config);
    }
}
