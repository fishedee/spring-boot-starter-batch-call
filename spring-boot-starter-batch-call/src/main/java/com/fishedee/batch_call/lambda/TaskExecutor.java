package com.fishedee.batch_call.lambda;

import com.fishedee.batch_call.InvalidCallResultException;

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
            throw new InvalidCallResultException("Call Result size "+objectList.size()+" is not equal to task size "+tasks.size());
        }
        return objectList;
    }

    public List<List<Object>> invokeKeyMatch(Config config,List<Task> tasks){
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
        //将结果写入到List
        List<List<Object>> finalResultList = new ArrayList<>();
        for( Task task :tasks ){
            List<Object> singleResult = mapResult.get(task.getKey());
            if( singleResult == null ){
                singleResult = new ArrayList<>();
            }
            finalResultList.add(singleResult);
        }
        return finalResultList;
    }
}
