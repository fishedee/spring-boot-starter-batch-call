package com.fishedee.batch_call;

import com.fishedee.batch_call.autoconfig.BatchCallProperties;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

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

    private int getBatchSize(int batchSize){
        //手动配置优先
        if( batchSize > 0 ){
            return batchSize;
        }
        //配置文件次之
        batchSize = properties.getBatchSize();
        if( batchSize > 0 ){
            return batchSize;
        }
        //保底逻辑
        return 100;
    }

    private List<Task> singleBatch(Config config,List<Task> taskList,TaskCache cache){
        boolean cacheEnabled = config.isCacheEnabled();
        boolean hasDispatcherFunc = config.isHasDispatchFunc();
        List<Task> nextStepTask = new ArrayList<>();
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
                taskCacheResult.setCacheResult(new ArrayList<>());
                taskCacheResult.setHasCacheTask(new ArrayList<>());
            }
            if( taskCacheResult.getHasCacheTask().size()!= 0 && hasDispatcherFunc){
                //对有缓存的部分，直接进行数据分发
                List<Object> dispatchResult = dispatcher.dispatchKeyMatch(config,taskCacheResult.getHasCacheTask(),taskCacheResult.getCacheResult());
                if( dispatchResult.size() != 0 ){
                    nextStepTask.addAll(finder.find(config,dispatchResult));
                }
            }
            if( taskCacheResult.getNoCacheTask().size() != 0 ){
                //对没有缓存的部分，先执行批量调用
                List<List<Object>> result = executor.invokeKeyMatch(config,taskCacheResult.getNoCacheTask());
                if( cacheEnabled ){
                    //开启缓存的情况下，将数据放入缓存
                    cache.putAll(taskCacheResult.getNoCacheTask(),result);
                }
                if( hasDispatcherFunc ) {
                    //有数据分发操作
                    List<Object> dispatchResult = dispatcher.dispatchKeyMatch(config, taskCacheResult.getNoCacheTask(), result);
                    if (dispatchResult.size() != 0) {
                        nextStepTask.addAll(finder.find(config, dispatchResult));
                    }
                }
            }
        }else{
            //连续匹配的方式，不能开启缓存
            //不开启缓存的话，直接进行批量调用与分发操作
            List<Object> result = executor.invokeSequenceMatch(config,taskList);
            if( hasDispatcherFunc ){
                //有数据分发操作
                List<Object> dispatchResult = dispatcher.dispatchSequenceMatch(config,taskList,result);
                if( dispatchResult.size() != 0 ){
                    nextStepTask.addAll(finder.find(config,dispatchResult));
                }
            }

        }
        return nextStepTask;
    }

    public void run(Config config,Object target){
        //每个任务一个单独的cache
        TaskCache cache = new TaskCache();
        List<Task> taskList = finder.find(config,target);
        int batchSize = this.getBatchSize(config.getBatchSize());

        while( taskList.size() != 0){
            //只允许同一种Class，同一个Field的任务在运行
            List<Task> nextStepTask = new ArrayList<>();

            //分批执行
            int taskListBegin = 0;
            while( taskListBegin < taskList.size()){
                int taskListEnd = taskList.size();
                if( taskListEnd - taskListBegin > batchSize) {
                    taskListEnd = taskListBegin + batchSize;
                }
                //执行单个批次
                List<Task> currentTask = taskList.subList(taskListBegin,taskListEnd);
                List<Task> currentNextStepTask = this.singleBatch(config,currentTask,cache);

                //调整下一个批次的起点，以及添加nextStepTask
                taskListBegin = taskListEnd;
                nextStepTask.addAll(currentNextStepTask);
            }

            //转换到下一步的任务列表
            taskList = nextStepTask;
        }
    }
}
