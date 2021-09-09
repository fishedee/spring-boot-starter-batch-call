package com.fishedee.batch_call;

public class BatchCallRun{

    private Config config;

    public BatchCallRun(Config config){
        this.config = config;
    }

    public BatchCallRun setBatchSize(int batchSize){
        this.config.setBatchSize(batchSize);
        return this;
    }

    public BatchCallRun setCacheEnabled(boolean cacheEnabled){
        this.config.setCacheEnabled(cacheEnabled);
        return this;
    }

    public void run(Object target){
        TaskRunner.sinlegton().run(this.config,target);
    }

}
