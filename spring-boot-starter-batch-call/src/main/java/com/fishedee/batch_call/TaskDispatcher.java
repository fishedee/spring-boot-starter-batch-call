package com.fishedee.batch_call;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@Slf4j
public class TaskDispatcher {
    public List<Object> dispatchKeyMatch(Config config,List<Task> taskList, List<List<Object>> resultList){
        List<Object> nextStepList = new ArrayList<>();

        boolean allowResultNotFound = config.isHasCallResultMatchByKeyDefault();
        String debugName = config.getCallTarget().getClass().getName();

        //回调数据
        BiFunction dispatchFunc = config.getDispatchFunc();
        Object defaultResult = config.getCallResultMatchByKeyDefault();
        for( int i = 0 ;i != taskList.size();i++){
            Task task = taskList.get(i);
            List<Object> result = resultList.get(i);
            Object nextStep = null;
            if( config.isDipatchFuncArguListType() ){
                //参数是List类型
                nextStep = dispatchFunc.apply(task.getInstance(),result);
            }else{
                //参数不是List类型
                if( result.size() == 1 ) {
                    nextStep = dispatchFunc.apply(task.getInstance(),result.get(0));
                }else if( result.size() == 0  ){
                    if( allowResultNotFound ){
                        nextStep = dispatchFunc.apply(task.getInstance(),defaultResult);
                    }else{
                        throw new CallResultNotFoundException(debugName,task.getKey());
                    }
                }else if( result.size() != 1 ){
                    throw new CallResultMultiplyConfuseException(debugName,task.getKey());
                }
            }
            if( nextStep != null ){
                nextStepList.add(nextStep);
            }
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
            if( nextStep != null ){
                nextStepList.add(nextStep);
            }
        }
        return nextStepList;
    }
}
