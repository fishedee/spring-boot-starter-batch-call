package com.fishedee.batch_call;

import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskExecutor implements BeanFactoryAware {

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory factory) throws BeansException{
        this.beanFactory = factory;
    }

    public List<Object> invokeSequenceMatch(List<Task> tasks){
        Task.Config config = tasks.get(0).getConfig();

        //聚合数据
        List<Object> invokeArguments = new ArrayList<>();
        for( Task task :tasks){
            invokeArguments.add(task.getKey());
        }

        //批量调用
        try {
            Object invokeBean = this.beanFactory.getBean(config.getInvokeTarget());
            Object result = config.getInvokeMethod().invoke(invokeBean,invokeArguments);
            //获取结果
            List<Object> objectList = (List<Object>)result;
            if( objectList.size() != tasks.size() ){
                throw new InvalidCallResultException("Call Result size "+objectList.size()+" is not equal to task size "+tasks.size());
            }
            return objectList;
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

    public List<List<Object>> invokeKeyMatch(List<Task> tasks){
        Task.Config config = tasks.get(0).getConfig();

        //聚合数据
        List<Object> invokeArguments = new ArrayList<>();
        for( Task task :tasks){
            invokeArguments.add(task.getKey());
        }

        //批量调用
        try{
            Object invokeBean = this.beanFactory.getBean(config.getInvokeTarget());
            Object result = config.getInvokeMethod().invoke(invokeBean,invokeArguments);
            //获取结果
            List<Object> resultList = (List<Object>)result;
            //将结果转换为map
            Map<Object,List<Object>> mapResult = new HashMap<>();
            for( Object singleResult : resultList){
                Object singleResultKey = config.getResultKeyMethod().invoke(singleResult);
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
}
