package com.fishedee.batch_call.lambda;

import com.fishedee.batch_call.BatchCall;
import com.fishedee.batch_call.ResultMatch;
import com.fishedee.batch_call.Task;
import com.fishedee.batch_call.TaskCache;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
