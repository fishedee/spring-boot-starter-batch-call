package com.fishedee.batch_call;

import com.fishedee.batch_call.autoconfig.BatchCallProperties;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Function;

public class TaskRunner {
    @Autowired
    private BatchCallProperties properties;

    @Autowired
    private TaskFinder finder;

    @Autowired
    private TaskExecutor executor;

    @Autowired
    private TaskDispatcher dispatcher;

    private static TaskRunner globalTaskRunner;

    public static TaskRunner sinlegton(){
        return globalTaskRunner;
    }

    @PostConstruct
    public void init(){
        globalTaskRunner = this;
    }

    public BatchCallProperties getProperties(){
        return this.properties;
    }

    private void singleBatch(Config config,List<Task> taskList,TaskCache cache,FunctionVoid<List<Object>> nextStepCallback){
        boolean cacheEnabled = config.isCacheEnabled();
        boolean hasDispatcherFunc = config.isHasDispatchFunc();
        boolean isCallMode = config.isCallMode();
        if( config.getMatcher() == ResultMatch.KEY){
            //使用key匹配的方式
            TaskCache.Result taskCacheResult;
            if( cacheEnabled ){
                //开启缓存
                taskCacheResult = cache.getAll(taskList);
            }else{
                //不开启缓存
                taskCacheResult = new TaskCache.Result();
                taskCacheResult.setNoCacheTask(taskList);
                taskCacheResult.setHasCacheTask(new ArrayList<>());
            }
            if( taskCacheResult.getHasCacheTask().size()!= 0 && hasDispatcherFunc){
                //对有缓存的部分，直接进行数据分发
                List<Object> dispatchResult = dispatcher.dispatchKeyMatch(config,taskCacheResult.getHasCacheTask(),taskCacheResult.getCacheResult());
                nextStepCallback.apply(dispatchResult);
            }
            if( taskCacheResult.getNoCacheTask().size() != 0 ){
                Map<Object,List<Object>> result;
                if( isCallMode ){
                    //对没有缓存的部分，先执行批量调用
                    result = executor.invokeKeyMatch(config,taskCacheResult.getNoCacheTask());
                }else{
                    //不能调用的情况下，设置默认值
                    result = new HashMap<>();
                    for( Task task : taskCacheResult.getNoCacheTask() ){
                        result.put(task.getKey(),new ArrayList<>());
                    }
                }
                if( cacheEnabled ){
                    //开启缓存的情况下，将数据放入缓存
                    cache.putAll(result);
                }
                if( hasDispatcherFunc ) {
                    //有数据分发操作
                    List<Object> dispatchResult = dispatcher.dispatchKeyMatch(config, taskCacheResult.getNoCacheTask(), result);
                    nextStepCallback.apply(dispatchResult);
                }
            }
        }else{
            //连续匹配的方式，不能开启缓存
            //不开启缓存的话，直接进行批量调用与分发操作
            List<Object> result = executor.invokeSequenceMatch(config,taskList);
            if( hasDispatcherFunc ){
                //有数据分发操作
                List<Object> dispatchResult = dispatcher.dispatchSequenceMatch(config,taskList,result);
                nextStepCallback.apply(dispatchResult);
            }
        }
    }

    private void initCache(Config config,TaskCache taskCache){
        if( config.isCallMode() ){
            return;
        }
        //在查找模式中，首先将数据导入到cache中
        List<Object> findResult = config.getFindResult();
        Function findResultMatchByKey = config.getCallResultMatchByKey();
        Map<Object,List<Object>> cache = new HashMap<>();
        for( Object value : findResult ){
            Object key = findResultMatchByKey.apply(value);
            List<Object> resultInCache = cache.get(key);
            if( resultInCache == null ){
                resultInCache = new ArrayList<>();
                cache.put(key,resultInCache);
            }
            resultInCache.add(value);
        }
        taskCache.putAll(cache);
    }

    public void run(Config config,Object target){
        //每个任务一个单独的cache
        TaskCache cache = new TaskCache();
        this.initCache(config,cache);
        List<Task> initTaskList = finder.find(config,target);
        Queue<Task> nextStepTask = new ArrayDeque<>();
        nextStepTask.addAll(initTaskList);
        int batchSize = config.getBatchSize();

        while( nextStepTask.size() != 0){
            //分批执行
            //执行单个批次
            List<Task> currentTask = new ArrayList<>();
            if( nextStepTask.size() < batchSize){
                currentTask.addAll(nextStepTask);
                nextStepTask.clear();
            }else{
                for( int i = 0 ;i != batchSize;i++){
                    currentTask.add(nextStepTask.remove());
                }
            }
            this.singleBatch(config,currentTask,cache,(nextStepList)->{
                //调整下一个的nextStepTask
                List<Task> newTask = finder.find(config,nextStepList);
                nextStepTask.addAll(newTask);
            });
        }
    }
}
