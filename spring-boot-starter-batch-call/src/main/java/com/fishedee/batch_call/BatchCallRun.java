package com.fishedee.batch_call;

import java.util.List;
import java.util.function.Function;

public class BatchCallRun<KeyType,CallResultType>{

    private Config config;

    public BatchCallRun(Config config){
        this.config = config;
    }

    public BatchCallRun<KeyType,CallResultType> setBatchSize(int batchSize){
        if( batchSize <= 0 ){
            throw new InvalidArgumentException("batchSize ["+batchSize+"] must be positive");
        }
        this.config.setBatchSize(batchSize);
        return this;
    }

    public BatchCallRun<KeyType,CallResultType> setCacheEnabled(boolean cacheEnabled){
        this.config.setCacheEnabled(cacheEnabled);
        return this;
    }

    public void run(Object target){
        this.config.setFirstCallThenRun(false);
        TaskRunner.sinlegton().directRun(this.config,target);
    }

    public<InitRunType> List<InitRunType> firstCallThenRun(List<KeyType> firstCallArgv, Function<CallResultType,KeyType> firstCallGetResultKey,Function<CallResultType,InitRunType> firstCallGetResultConvert){
        this.config.setFirstCallThenRun(true);
        this.config.setFirstCallArgu((List<Object>)firstCallArgv);
        this.config.setFirstCallGetResultKey(firstCallGetResultKey);
        this.config.setFirstCallGetResultConvert(firstCallGetResultConvert);
        return (List<InitRunType>)TaskRunner.sinlegton().firstCallThenRun(this.config);
    }
}
