package com.fishedee.batch_call;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TaskFinder {
    @Data
    public static class ClassInfo{
        private List<Method> maybeKeyMethod = new ArrayList<>();
    }

    private final Map<Class, ClassInfo> configMap;

    public TaskFinder(){
        this.configMap = new ConcurrentHashMap<>();
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

    private Method tryGetterMethod(Class clazz, Field field){
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

    private void calcuateClassFieldInfo(Class clazz, ClassInfo result){
        //先去查找Field
        Class parentClazz = clazz;
        while( parentClazz != Object.class){
            Field[] fields = parentClazz.getDeclaredFields();
            for( Field field :fields ){
                //找不到BatchCall
                Class fieldClass = field.getType();
                //集合类型的话我们就尝试一下进行嵌套查找
                if( List.class.isAssignableFrom(fieldClass) ||
                        Set.class.isAssignableFrom(fieldClass) ||
                        Map.class.isAssignableFrom(fieldClass)){
                    log.info("container {}",fieldClass);
                    //尝试获取对应的方法
                    Method method = tryGetterMethod(clazz,field);
                    if( method == null ){
                        //找不到就略过吧，不用报错，因为这个不是注解的
                        log.info("tryGetterMethod error {}",fieldClass);
                        continue;
                    }
                    Method method2 = this.isValidKeyMethod(method);
                    if( method2 == null){
                        //不合法就略过吧，不用报错，因为这个不是注解
                        log.info("isValidKeyMethod error {}",fieldClass);

                        continue;
                    }
                    result.maybeKeyMethod.add(method2);
                }
            }
            parentClazz = parentClazz.getSuperclass();
        }
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
            classInfo = new ClassInfo();
            this.calcuateClassFieldInfo(clazz,classInfo);
            this.configMap.putIfAbsent(clazz,classInfo);
            return classInfo;
        }
    }


    private void findInner(Config config,Object target,List<Task> result){
        if( target == null ){
            return;
        }
        Class clazz = target.getClass();
        if( List.class.isAssignableFrom(clazz)){
            log.info("list type {} classInfo {}",clazz);
            //List类型
            List targetList = (List)target;
            for( Object single : targetList){
                findInner(config,single,result);
            }
        }else if (Set.class.isAssignableFrom(clazz)){
            //Set类型
            Set targetSet = (Set)target;
            for( Object single : targetSet){
                findInner(config,single,result);
            }
        }else if( Map.class.isAssignableFrom(clazz)){
            //Map类型
            Map targetMap = (Map)target;
            //只判断value，不需要判断key，改了key会改变Map的语义
            for(Object single :targetMap.values()){
                findInner(config,single,result);
            }
        }else{

            //非集合类型
            ClassInfo classInfo = this.getClassInfo(clazz);

            log.info("normal type {} classInfo {}",clazz,classInfo);
            //提取自身的数据
            if( clazz == config.getKeyObjectType() ){
                Object keyInstance = config.getCollectFunc().apply(target);
                Task task = new Task();
                task.setInstance(target);
                task.setKey(keyInstance);
                result.add(task);
            }
            //筛选其他字段
            for( Method method : classInfo.getMaybeKeyMethod() ){
                try{
                    Object nestedTarget = method.invoke(target);
                    //嵌套进去继续查找
                    findInner(config,nestedTarget,result);
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

    public List<Task> find(Config config,Object target){
        List<Task> result = new ArrayList<>();
        this.findInner(config,target,result);
        return result;
    }
}
