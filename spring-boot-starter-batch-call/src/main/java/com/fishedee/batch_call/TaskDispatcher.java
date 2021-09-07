package com.fishedee.batch_call;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TaskDispatcher {
    public List<Object> dispatch(List<Task> taskList,List<Object> resultList){
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
