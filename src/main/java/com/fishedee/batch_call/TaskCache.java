package com.fishedee.batch_call;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TaskCache {
    @Data
    public static class Result{
        private List<Task> hasCacheTask;
        private Map<Object,List<Object>> cacheResult;
        private List<Task> noCacheTask;
    }

    private Map<Object,List<Object>> cacheMap;

    public TaskCache(){
        this.cacheMap = new HashMap<>();
    }

    public Result getAll(List<Task> tasks){
        List<Task> hasCacheTask= new ArrayList<>();
        List<Task> noCacheTask = new ArrayList<>();
        for( Task task :tasks){
            Object key = task.getKey();
            List<Object> cacheData = this.cacheMap.get(key);
            if( cacheData == null ){
                noCacheTask.add(task);
            }else{
                hasCacheTask.add(task);
            }
        }
        Result result = new Result();
        result.setHasCacheTask(hasCacheTask);
        result.setCacheResult(this.cacheMap);
        result.setNoCacheTask(noCacheTask);
        return result;
    }

    public void putAll(Map<Object,List<Object>> result){
        this.cacheMap.putAll(result);
    }
}
