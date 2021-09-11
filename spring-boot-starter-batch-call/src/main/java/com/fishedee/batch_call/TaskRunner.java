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
        Function callResultMatchByKey = config.getCallResultMatchByKey();
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
                //对没有缓存的部分，先执行批量调用
                Map<Object,List<Object>> result = executor.invokeKeyMatch(config,taskCacheResult.getNoCacheTask(),callResultMatchByKey);
                if( cacheEnabled ){
                    //开启缓存的情况下，将数据放入缓存
                    cache.putAll(taskCacheResult.getNoCacheTask(),result);
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

    public void directRun(Config config, Object data){
        TaskCache cache = new TaskCache();
        run(config,data,cache);
    }

    private List<Object> firstCall(TaskCache cache,Config config){
        boolean cacheEnabled = config.isCacheEnabled();
        Function firstCallGetResultKey = config.getFirstCallGetResultKey();
        Function firstCallGetResultConvert = config.getFirstCallGetResultConvert();
        //创建首次任务
        List<Task> taskList = new ArrayList<>();
        for( Object argv : config.getFirstCallArgu() ){
            Task task = new Task();
            task.setKey(argv);
            taskList.add(task);
        }

        //执行批量调用
        Map<Object,List<Object>> result = executor.invokeKeyMatch(config,taskList,firstCallGetResultKey);

        if( cacheEnabled ){
            //开启缓存的情况下，将数据放入缓存
            cache.putAll(taskList,result);
        }

        //结果转换
        Class targetClass = config.getCallTarget().getClass();
        List<Object> convertResult = new ArrayList<>();
        for( Task task :taskList){
            List<Object> singleResult = result.get(task.getKey());
            if( singleResult.size() == 0  ){
                throw new CallResultNotFoundException(targetClass,task.getKey());
            }else if( singleResult.size() < 0 ){
                throw new CallResultMultiplyConfuseException(targetClass,task.getKey());
            }else{
                convertResult.add(firstCallGetResultConvert.apply(singleResult));
            }
        }
        return convertResult;
    }

    public List<Object> firstCallThenRun(Config config){
        TaskCache cache = new TaskCache();
        List<Object> target = this.firstCall(cache,config);
        this.run(config,target,cache);
        return target;
    }

    private void run(Config config,Object target,TaskCache cache){
        //每个任务一个单独的cache
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
