package com.fishedee.batch_call;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TaskDispatcher {
    public List<Object> dispatchKeyMatch(List<Task> taskList,List<List<Object>> resultList){
        List<Object> nextStepList = new ArrayList<>();

        Task.Config config = taskList.get(0).getConfig();
        boolean allowResultNotFound = config.getBatchCall().allowResultNotFound();
        String debugName = config.getBatchCall().name();

        //回调数据
        Method callbackMethod = config.getCallbackMethod();
        for( int i = 0 ;i != taskList.size();i++){
            Task task = taskList.get(i);
            List<Object> result = resultList.get(i);
            try{
                Object nextStep = null;
                if( config.isCallbackMethodArgumentIsListType() ){
                    //参数是List类型
                    nextStep = callbackMethod.invoke(task.getInstance(),result);
                }else{
                    //参数不是List类型
                    if( result.size() == 1 ) {
                        nextStep = callbackMethod.invoke(task.getInstance(),result.get(0));
                    }else if( result.size() == 0  ){
                        if( allowResultNotFound ){
                            nextStep = callbackMethod.invoke(task.getInstance(),null);
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
            }catch( InvocationTargetException e ){
                Throwable cause = e.getCause();
                if( cause instanceof  RuntimeException){
                    throw (RuntimeException)cause;
                }else{
                    throw new InvokeReflectMethodException(cause);
                }
            }catch(IllegalAccessException e){
                throw new InvokeReflectMethodException(e);
            }catch(IllegalArgumentException e){
                throw new InvokeReflectMethodException(e);
            }
        }
        return nextStepList;
    }

    public List<Object> dispatchSequenceMatch(List<Task> taskList,List<Object> resultList){
        List<Object> nextStepList = new ArrayList<>();

        //回调数据
        Task.Config config = taskList.get(0).getConfig();
        Method callbackMethod = config.getCallbackMethod();
        for( int i = 0 ;i != taskList.size();i++){
            Task task = taskList.get(i);
            Object result = resultList.get(i);
            try{
                Object nextStep = callbackMethod.invoke(task.getInstance(),result);
                if( nextStep != null ){
                    nextStepList.add(nextStep);
                }
            }catch( InvocationTargetException e ){
                Throwable cause = e.getCause();
                if( cause instanceof  RuntimeException){
                    throw (RuntimeException)cause;
                }else{
                    throw new InvokeReflectMethodException(cause);
                }
            }catch(IllegalAccessException e){
                throw new InvokeReflectMethodException(e);
            }catch(IllegalArgumentException e){
                throw new InvokeReflectMethodException(e);
            }
        }
        return nextStepList;
    }
}
