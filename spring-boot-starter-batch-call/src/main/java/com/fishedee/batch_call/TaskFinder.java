package com.fishedee.batch_call;

import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TaskFinder {


    private final Map<Class, ClassTaskResolver.ClassInfo> configMap;

    public TaskFinder(){
        this.configMap = new ConcurrentHashMap<>();
    }

    //这里是public是为了为linter做准备
    private ClassTaskResolver.ClassInfo getClassInfo(Class clazz){
        ClassTaskResolver.ClassInfo classInfo;
        classInfo = this.configMap.get(clazz);
        if( classInfo != null ){
            return classInfo;
        }
        //注意，这里需要二次查找，因为可能同时有多个不匹配的，就会同时到达这里面
        //但最终只有一个进入了synchronized里面进行拿原始值，所以其他的只需要找缓存值就可以了。
        synchronized (this.configMap){
            classInfo = this.configMap.get(clazz);
            if( classInfo != null ){
                return classInfo;
            }
            classInfo = new ClassTaskResolver(clazz).resolve();
            this.configMap.putIfAbsent(clazz,classInfo);
            return classInfo;
        }
    }


    private void addResult(List<Task> result,Task.Config config,Object instance){
        Method keyMethod = config.getGetKeyMethod();
        try{
            Object key = keyMethod.invoke(instance);
            Task task = new Task();
            task.setKey(key);
            task.setInstance(instance);
            task.setConfig(config);
            result.add(task);
        }catch( InvocationTargetException e){
            Throwable cause = e.getCause();
            if( cause instanceof  RuntimeException){
                throw (RuntimeException)cause;
            }else{
                throw new InvokeReflectMethodException(cause);
            }
        }catch( IllegalAccessException e){
            throw new InvokeReflectMethodException(e);
        }catch( IllegalArgumentException e){
            throw new InvokeReflectMethodException(e);
        }
    }

    private void findInner(String taskName,Object target,List<Task> result){
        Class clazz = target.getClass();
        if( List.class.isAssignableFrom(clazz)){
            log.info("list type {} classInfo {}",clazz);
            //List类型
            List targetList = (List)target;
            for( Object single : targetList){
                findInner(taskName,single,result);
            }
        }else if (Set.class.isAssignableFrom(clazz)){
            //Set类型
            Set targetSet = (Set)target;
            for( Object single : targetSet){
                findInner(taskName,single,result);
            }
        }else if( Map.class.isAssignableFrom(clazz)){
            //Map类型
            Map targetMap = (Map)target;
            //只判断value，不需要判断key，改了key会改变Map的语义
            for(Object single :targetMap.values()){
                findInner(taskName,single,result);
            }
        }else{

            //非集合类型
            ClassTaskResolver.ClassInfo classInfo = this.getClassInfo(clazz);
            log.info("normal type {} classInfo {}",clazz,classInfo);
            //筛选符合taskName的字段
            for(Task.Config config : classInfo.getConfig()){
                if( config.getBatchCall().task().trim().equals(taskName) ) {
                    addResult(result, config, target);
                }
            }
            //筛选其他字段
            for( Method method : classInfo.getMaybeKeyMethod() ){
                try{
                    Object nestedTarget = method.invoke(target);
                    //嵌套进去继续查找
                    findInner(taskName,nestedTarget,result);
                }catch(InvocationTargetException e){
                    Throwable cause = e.getCause();
                    if( cause instanceof RuntimeException){
                        throw (RuntimeException)cause;
                    }else{
                        throw new InvokeReflectMethodException(cause);
                    }
                }catch(IllegalArgumentException e){
                    throw new InvokeReflectMethodException(e);
                }catch(IllegalAccessException e){
                    throw new InvokeReflectMethodException(e);
                }
            }
        }
    }

    public List<Task> find(String taskName,Object target){
        List<Task> result = new ArrayList<>();
        this.findInner(taskName,target,result);
        return result;
    }
}
