package com.fishedee.batch_call;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TaskFinder {

    private static class ClassInfo{
        List<Task.Config> config = new ArrayList<>();
        List<Method> maybeKeyMethod = new ArrayList<>();
    }
    private final Map<Class, ClassInfo> configMap;

    @Autowired
    private TaskChecker taskChecker;

    public TaskFinder(){
        this.configMap = new ConcurrentHashMap<>();
    }

    private Task.Config calcuateConfig(BatchCall annotation,Class clazz,Method method){
        return taskChecker.calcuateConfig(annotation,clazz,method);
    }

    private List<Task.Config> calcuateMultiplyConfig(MultipleBatchCall annotation,Class clazz,Method method){
        BatchCall[] batchCalls = annotation.value();
        List<Task.Config> result = new ArrayList<>();
        for( BatchCall single : batchCalls){
            result.add(this.calcuateConfig(single,clazz,method));
        }
        return result;
    }

    private Method isValidKeyMethod(Method method){
        //方法必须为0参数
        if( method.getParameterCount() != 0 ){
            return null;
        }
        //返回值必须不是void
        Class returnType = method.getReturnType();
        if( returnType == Void.class ){
            return null;
        }
        return method;
    }

    public Method tryGetterMethod(Class clazz,Field field){
        try{
            String methodName = GetterUtil.getGetterMethodName(field.getName());
            Method method = clazz.getMethod(methodName);
            if( method != null ){
                return method;
            }
            return null;
        }catch(NoSuchMethodException e){
            //找不到就下一个吧
            return null;
        }catch(SecurityException e){
            //找不到就下一个吧
            return null;
        }
    }

    private void calcuateClassFieldInfo(Class clazz,ClassInfo result){
        //先去查找Field
        Class parentClazz = clazz;
        while( parentClazz != Object.class){
            Field[] fields = parentClazz.getDeclaredFields();
            for( Field field :fields ){
                BatchCall batchCall = field.getAnnotation(BatchCall.class);
                MultipleBatchCall multipleBatchCall = field.getAnnotation(MultipleBatchCall.class);
                if( batchCall != null || multipleBatchCall != null ){
                    //注意这里一个字段可能同时匹配了BatchCall与MultipleBatchCall
                    if( batchCall != null ){
                        //找到了一个BatchCall
                        Method method = this.tryGetterMethod(clazz,field);
                        if( method == null ){
                            throw new BatchCallException("Could not found @BachCall "+ clazz.getName() +" -> "+field.getName()+" getter" );
                        }
                        Method method2 = this.isValidKeyMethod(method);
                        if( method2 == null){
                            throw new BatchCallException("@BachCall "+ clazz.getName() +" -> "+method.getName()+" is invalid " );
                        }
                        Task.Config config = calcuateConfig(batchCall,clazz,method2);
                        result.config.add(config);
                    }
                    if( multipleBatchCall != null){
                        //找到了一个MultiplyBatchCall
                        Method method = tryGetterMethod(clazz,field);
                        if( method == null ){
                            throw new BatchCallException("Could not found @MultipleBatchCall "+ clazz.getName() +" -> "+field.getName()+" getter or it is invalid" );
                        }
                        Method method2 = this.isValidKeyMethod(method);
                        if( method2 == null){
                            throw new BatchCallException("@BachCall "+ clazz.getName() +" -> "+method.getName()+" is invalid " );
                        }
                        List<Task.Config> config = calcuateMultiplyConfig(multipleBatchCall,clazz,method2);
                        result.config.addAll(config);
                    }
                }else{
                    //找不到BatchCall
                    Class fieldClass = field.getType();
                    //集合类型的话我们就尝试一下进行嵌套查找
                    if( List.class.isAssignableFrom(fieldClass) ||
                        Set.class.isAssignableFrom(fieldClass) ||
                        Map.class.isAssignableFrom(fieldClass)){
                        //尝试获取对应的方法
                        Method method = tryGetterMethod(clazz,field);
                        if( method == null ){
                            //找不到就略过吧，不用报错，因为这个不是注解的
                            continue;
                        }
                        Method method2 = this.isValidKeyMethod(method);
                        if( method2 == null){
                            //不合法就略过吧，不用报错，因为这个不是注解
                            continue;
                        }
                        result.maybeKeyMethod.add(method2);
                    }
                }
            }
            parentClazz = parentClazz.getSuperclass();
        }
    }

    private void calcuateClassMethodInfo(Class clazz,ClassInfo result){
        Method[] methods = clazz.getMethods();
        for( Method method :methods ){
            BatchCall batchCall = method.getAnnotation(BatchCall.class);
            MultipleBatchCall multipleBatchCall = method.getAnnotation(MultipleBatchCall.class);
            if( batchCall != null || multipleBatchCall != null ){
                //注意这里一个方法可能同时匹配了BatchCall与MultipleBatchCall
                if( batchCall != null ){
                    //找到了一个BatchCall
                    Method method2 = isValidKeyMethod(method);
                    if( method2 == null ){
                        throw new BatchCallException("@BachCall "+ clazz.getName() +" -> "+method.getName()+" is invalid" );
                    }
                    Task.Config config = calcuateConfig(batchCall,clazz,method2);
                    result.config.add(config);
                }
                if( multipleBatchCall != null){
                    Method method2 = isValidKeyMethod(method);
                    if( method2 == null ){
                        throw new BatchCallException("@MultipleBatchCall "+ clazz.getName() +" -> "+method.getName()+" is invalid" );
                    }
                    //找到了一个MultiplyBatchCall
                    List<Task.Config> config = calcuateMultiplyConfig(multipleBatchCall,clazz,method2);
                    result.config.addAll(config);
                }
            }
        }
    }

    private ClassInfo calcuateClassInfo(Class clazz){
        ClassInfo result = new ClassInfo();
        //查找Field信息
        this.calcuateClassFieldInfo(clazz,result);

        //查找Method信息
        this.calcuateClassMethodInfo(clazz,result);
        return result;
    }

    private ClassInfo getClassInfo(Class clazz){
        ClassInfo classInfo;
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
            classInfo = this.calcuateClassInfo(clazz);
            this.configMap.putIfAbsent(clazz,classInfo);
            return classInfo;
        }
    }


    private void addResult(List<Task> result,Task.Config config,Object instance){
        Method keyMethod = config.getKeyMethod;
        try{
            Object key = keyMethod.invoke(instance);
            Task task = new Task();
            task.key = key;
            task.instance = instance;
            task.config = config;
            result.add(task);
        }catch( InvocationTargetException e){
            throw (RuntimeException)e.getCause();
        }catch( Exception e){
            throw new BatchCallException("invoke method error ",e);
        }
    }

    private void findInner(String taskName,Object target,List<Task> result){
        Class clazz = target.getClass();
        if( List.class.isAssignableFrom(clazz)){
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
            ClassInfo classInfo = this.getClassInfo(clazz);
            //筛选符合taskName的字段
            for(Task.Config config : classInfo.config){
                if( config.batchCall.task().equals(taskName) ) {
                    addResult(result, config, target);
                }
            }
            //筛选其他字段
            for( Method method : classInfo.maybeKeyMethod ){
                try{
                    Object nestedTarget = method.invoke(target);
                    //嵌套进去继续查找
                    findInner(taskName,nestedTarget,result);
                }catch(InvocationTargetException e){
                    throw (RuntimeException)e.getCause();
                }catch(Exception e){
                    throw new BatchCallException("invoke method error",e);
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
