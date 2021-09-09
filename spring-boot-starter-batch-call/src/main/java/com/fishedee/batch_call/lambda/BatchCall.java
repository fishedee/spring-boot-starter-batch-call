package com.fishedee.batch_call.lambda;

import java.util.List;
import java.util.function.BiFunction;

public class BatchCall<KeyObjectType,KeyType> {

    private Config config;

    public BatchCall(Config config){
        this.config = config;
    }

    public <CallTargetType> BatchCallVoidDispatch<KeyObjectType> call(CallTargetType callTarget, BiFunctionVoid<CallTargetType, List<KeyType>> callFunc){
        this.config.setCallFunc(( Object a,Object b)->{
            callFunc.apply((CallTargetType)a,(List<KeyType>)b);
            return null;
        });
        this.config.setCallTarget(callTarget);
        this.config.setMatcher(ResultMatch.SEQUENCE);
        return new BatchCallVoidDispatch<KeyObjectType>(this.config);
    }

    public <CallTargetType,CallResultType> BatchCallDispatch<KeyObjectType,CallResultType> callForResult(CallTargetType callTarget, BiFunction<CallTargetType, List<KeyType>,List<CallResultType>> callFunc){
        this.config.setCallFunc(callFunc);
        this.config.setCallTarget(callTarget);
        this.config.setMatcher(ResultMatch.SEQUENCE);
        return new BatchCallDispatch<KeyObjectType,CallResultType>(this.config);
    }

    public <CallTargetType,CallResultType> BatchCallDispatch<KeyObjectType,CallResultType> callForResult(CallTargetType callTarget, BiFunction<CallTargetType, List<KeyType>,List<CallResultType>> callFunc,ResultMatchByKey<CallResultType,KeyType> matcherByKey){
        this.config.setCallFunc(callFunc);
        this.config.setCallTarget(callTarget);
        this.config.setMatcher(ResultMatch.KEY);
        this.config.setCallResultMatchByKey(matcherByKey.getMatcher());
        this.config.setHasCallResultMatchByKeyDefault(false);
        return new BatchCallDispatch<KeyObjectType,CallResultType>(this.config);
    }

    public <CallTargetType,CallResultType> BatchCallDispatch<KeyObjectType,CallResultType> callForResult(CallTargetType callTarget, BiFunction<CallTargetType, List<KeyType>,List<CallResultType>> callFunc,ResultMatchByKey<KeyType,CallResultType> matcherByKey,CallResultType defaultResult){
        this.config.setCallFunc(callFunc);
        this.config.setCallTarget(callTarget);
        this.config.setMatcher(ResultMatch.KEY);
        this.config.setCallResultMatchByKey(matcherByKey.getMatcher());
        this.config.setHasCallResultMatchByKeyDefault(true);
        this.config.setCallResultMatchByKeyDefault(defaultResult);
        return new BatchCallDispatch<KeyObjectType,CallResultType>(this.config);
    }

}
