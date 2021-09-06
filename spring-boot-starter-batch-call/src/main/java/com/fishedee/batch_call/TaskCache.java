package com.fishedee.batch_call;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskCache {
    public static class Result{
        List<Task> hasCacheTask;
        List<Object> cacheResult;
        List<Task> noCacheTask;
    }

    private Map<Object,Object> data;

    public TaskCache(){
        this.data = new HashMap<>();
    }

    public Result getAll(List<Task> tasks){
        return null;
    }

    public void putAll(List<Task> tasks,List<Object> result){

    }
}
