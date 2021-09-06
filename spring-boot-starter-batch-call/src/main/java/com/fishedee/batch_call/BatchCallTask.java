package com.fishedee.batch_call;

import com.fishedee.batch_call.autoconfig.BatchCallProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class BatchCallTask {
    @Autowired
    private TaskFinder finder;

    @Autowired
    private TaskExecutor executor;

    @Autowired
    private TaskDispatcher dispatcher;

    @Autowired
    private TaskChecker checker;

    @Autowired
    private BatchCallProperties properties;

    private int getBatchSize(BatchCall batchCall){
        //注解优先
        int batchSize = batchCall.batchSize();
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

    private List<Task> singleBatch(List<Task> taskList,TaskCache cache,String taskName){
        BatchCall batchCall = taskList.get(0).getConfig().batchCall;
        boolean cacheEnabled = batchCall.cacheEnabled();
        boolean isResultKeyMatch = (batchCall.resultMatch() == ResultMatch.KEY);

        List<Task> nextStepTask = new ArrayList<>();
        //开启缓存需要满足两个条件，缓存打开，以及使用key匹配的方式
        if( cacheEnabled && isResultKeyMatch){
            //开启缓存
            TaskCache.Result taskCacheResult = cache.getAll(taskList);
            if( taskCacheResult.hasCacheTask.size()!= 0 ){
                //对有缓存的部分，直接进行数据分发
                List<Object> dispatchResult = dispatcher.dispatch(taskCacheResult.hasCacheTask,taskCacheResult.cacheResult);
                if( dispatchResult.size() != 0 ){
                    nextStepTask.addAll(finder.find(taskName,dispatchResult));
                }
            }
            if( taskCacheResult.noCacheTask.size() != 0 ){
                //对没有缓存的部分，先执行批量调用
                List<Object> result = executor.invoke(taskCacheResult.noCacheTask);
                //将数据放入缓存
                cache.putAll(taskCacheResult.noCacheTask,result);
                //数据分发
                List<Object> dispatchResult = dispatcher.dispatch(taskCacheResult.noCacheTask,result);
                if( dispatchResult.size() != 0 ){
                    nextStepTask.addAll(finder.find(taskName,dispatchResult));
                }
            }
        }else{
            //不开启缓存的话，直接进行批量调用与分发操作
            List<Object> result = executor.invoke(taskList);
            List<Object> dispatchResult = dispatcher.dispatch(taskList,result);
            if( dispatchResult.size() != 0 ){
                nextStepTask.addAll(finder.find(taskName,dispatchResult));
            }
        }
        return nextStepTask;
    }

    public void run(String taskName,Object target){
        //每个任务一个单独的cache
        TaskCache cache = new TaskCache();
        List<Task> taskList = finder.find(taskName,target);

        while( taskList.size() != 0){
            //只允许同一种Class，同一个Field的任务在运行
            checker.check(taskList);

            //获取批次大小
            BatchCall batchCall = taskList.get(0).getConfig().batchCall;
            int batchSize = this.getBatchSize(batchCall);
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
                List<Task> currentNextStepTask = this.singleBatch(currentTask,cache,taskName);

                //调整下一个批次的起点，以及添加nextStepTask
                taskListBegin = taskListEnd;
                nextStepTask.addAll(currentNextStepTask);
            }

            //转换到下一步的任务列表
            taskList = nextStepTask;
        }
    }
}
