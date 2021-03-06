package com.fishedee.batch_call;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@Slf4j
public class TaskDispatcher {
    public List<Object> dispatchKeyMatch(Config config,List<Task> taskList, Map<Object,List<Object>> resultMap){
        List<Object> nextStepList = new ArrayList<>();

        boolean allowResultNotFound = config.isHasCallResultMatchByKeyDefault();
        Class targetClass = config.getCallTarget().getClass();

        //回调数据
        BiFunction dispatchFunc = config.getDispatchFunc();
        for( int i = 0 ;i != taskList.size();i++){
            Task task = taskList.get(i);
            List<Object> result = resultMap.get(task.getKey());
            Object nextStep = null;
            if( config.isDipatchFuncArguListType() ){
                //参数是List类型
                nextStep = dispatchFunc.apply(task.getInstance(),result);
            }else{
                //参数不是List类型
                if( result.size() == 0  ){
                    if( allowResultNotFound ){
                        //找不到的情况下，调用默认值函数，并且放入到Map中
                        Function defaultResultCall = config.getCallResultMatchByKeyDefault();
                        Object defaultResult = defaultResultCall.apply(task.getInstance());
                        result.add(defaultResult);
                        nextStep = dispatchFunc.apply(task.getInstance(),defaultResult);
                    }else{
                        throw new CallResultNotFoundException(targetClass,task.getKey());
                    }
                }else if( result.size() == 1 ) {
                    nextStep = dispatchFunc.apply(task.getInstance(),result.get(0));
                }else {
                    throw new CallResultMultiplyConfuseException(targetClass,task.getKey());
                }
            }
            nextStepList.add(nextStep);
        }
        return nextStepList;
    }

    public List<Object> dispatchSequenceMatch(Config config,List<Task> taskList, List<Object> resultList){
        List<Object> nextStepList = new ArrayList<>();

        //回调数据
        BiFunction dispatchFunc = config.getDispatchFunc();
        for( int i = 0 ;i != taskList.size();i++){
            Task task = taskList.get(i);
            Object result = null;
            if( resultList != null ){
                result = resultList.get(i);
            }
            Object nextStep = dispatchFunc.apply(task.getInstance(),result);
            nextStepList.add(nextStep);
        }
        return nextStepList;
    }
}
