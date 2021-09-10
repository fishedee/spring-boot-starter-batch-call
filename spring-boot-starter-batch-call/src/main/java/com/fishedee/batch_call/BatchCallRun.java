package com.fishedee.batch_call;

public class BatchCallRun<KeyType,ReturnType>{

    private Config config;

    public BatchCallRun(Config config){
        this.config = config;
    }

    public BatchCallRun<KeyType,ReturnType> setBatchSize(int batchSize){
        if( batchSize <= 0 ){
            throw new InvalidArgumentException("batchSize ["+batchSize+"] must be positive");
        }
        this.config.setBatchSize(batchSize);
        return this;
    }

    public BatchCallRun<KeyType,ReturnType> setCacheEnabled(boolean cacheEnabled){
        this.config.setCacheEnabled(cacheEnabled);
        return this;
    }

    public void run(Object target){
        this.config.setFirstSkipCollectAndThenCall(false);
        TaskRunner.sinlegton().collectAndThenCall(this.config,target);
    }

    public BatchCallDirectRun<ReturnType> firstSkipCollectAndThenCall(KeyType object){
        this.config.setFirstSkipCollectAndThenCall(true);
        this.config.setFirstCallArgu(object);
        return new BatchCallDirectRun<ReturnType>(this.config);
    }
}
