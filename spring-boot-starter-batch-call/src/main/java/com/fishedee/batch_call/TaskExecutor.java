package com.fishedee.batch_call;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskExecutor {

    public List<Object> invokeSequenceMatch(Config config,List<Task> tasks){
        //聚合数据
        List<Object> invokeArguments = new ArrayList<>();
        for( Task task :tasks){
            invokeArguments.add(task.getKey());
        }

        //批量调用
        Object invokeBean = config.getCallTarget();
        Object result = config.getCallFunc().apply(invokeBean,invokeArguments);
        if( result == null ){
            //触发返回值为空
            return null;
        }
        //获取结果
        List<Object> objectList = (List<Object>)result;
        if( objectList.size() != tasks.size() ){
            throw new CallResultSizeNotEqualException(invokeBean.getClass(),tasks.size(),objectList.size());
        }
        return objectList;
    }

    public Map<Object,List<Object>> invokeKeyMatch(Config config,List<Task> tasks){
        //聚合数据
        List<Object> invokeArguments = new ArrayList<>();
        for( Task task :tasks){
            invokeArguments.add(task.getKey());
        }

        //批量调用
        Object invokeBean = config.getCallTarget();
        Object result = config.getCallFunc().apply(invokeBean,invokeArguments);
        //获取结果
        List<Object> resultList = (List<Object>)result;
        //将结果转换为map
        Map<Object,List<Object>> mapResult = new HashMap<>();
        for( Object singleResult : resultList){
            Object singleResultKey = config.getCallResultMatchByKey().apply(singleResult);
            List<Object> valueList = mapResult.get(singleResultKey);
            if( valueList == null ){
                valueList = new ArrayList<>();
                mapResult.put(singleResultKey,valueList);
            }
            valueList.add(singleResult);
        }

        //对于没有找到的也要写入到mapResult里面
        //是为了让这一部分也能成为缓存，找不到也是一个值，需要写入缓存的
        for( Task task:tasks){
            boolean hasFound = mapResult.containsKey(task.getKey());
            if( hasFound == false ){
                mapResult.put(task.getKey(),new ArrayList<>());
            }
        }
        return mapResult;
    }
}
