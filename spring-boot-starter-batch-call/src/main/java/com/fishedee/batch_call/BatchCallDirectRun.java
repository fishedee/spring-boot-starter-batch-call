package com.fishedee.batch_call;

public class BatchCallDirectRun<ReturnType> {

    private Config config;

    public BatchCallDirectRun(Config config){
        this.config = config;
    }

    public ReturnType run(){
        return (ReturnType)TaskRunner.sinlegton().skipCollectAndThenCall(this.config,this.config.getFirstCallArgu());
    }
}
