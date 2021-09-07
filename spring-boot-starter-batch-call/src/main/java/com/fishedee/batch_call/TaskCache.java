package com.fishedee.batch_call;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskCache {
    @Data
    public static class Result{
        private List<Task> hasCacheTask;
        private List<List<Object>> cacheResult;
        private List<Task> noCacheTask;
    }

    private Map<Object,List<Object>> cacheMap;

    public TaskCache(){
        this.cacheMap = new HashMap<>();
    }

    public Result getAll(List<Task> tasks){
        List<Task> hasCacheTask= new ArrayList<>();
        List<List<Object>> cacheResult = new ArrayList<>();
        List<Task> noCacheTask = new ArrayList<>();
        for( Task task :tasks){
            Object key = task.getKey();
            List<Object> cacheData = this.cacheMap.get(key);
            if( cacheData == null ){
                noCacheTask.add(task);
            }else{
                hasCacheTask.add(task);
                cacheResult.add(cacheData);
            }
        }
        Result result = new Result();
        result.setHasCacheTask(hasCacheTask);
        result.setCacheResult(cacheResult);
        result.setNoCacheTask(noCacheTask);
        return result;
    }

    public void putAll(List<Task> tasks,List<List<Object>> result){
        for( int i = 0 ;i != tasks.size();i++){
            Object key = tasks.get(i).getKey();
            List<Object> value = result.get(i);
            this.cacheMap.put(key,value);
        }
    }
}
