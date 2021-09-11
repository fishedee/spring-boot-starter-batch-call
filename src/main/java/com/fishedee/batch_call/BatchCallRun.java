package com.fishedee.batch_call;

public class BatchCallRun{

    private Config config;

    public BatchCallRun(Config config){
        this.config = config;
    }

    public BatchCallRun setBatchSize(int batchSize){
        if( batchSize <= 0 ){
            throw new InvalidArgumentException("batchSize ["+batchSize+"] must be positive");
        }
        this.config.setBatchSize(batchSize);
        return this;
    }

    public BatchCallRun setCacheEnabled(boolean cacheEnabled){
        if( this.config.isCallMode() == false && cacheEnabled == false ){
            //查找数据数据的情况下，必须打开缓存
            throw new InvalidArgumentException("In find mode ,cacheEnabled must be true");
        }
        this.config.setCacheEnabled(cacheEnabled);
        return this;
    }

    public void run(Object target){
        TaskRunner.sinlegton().run(this.config,target);
    }
}
