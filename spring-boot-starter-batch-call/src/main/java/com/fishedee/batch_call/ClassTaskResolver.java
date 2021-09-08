package com.fishedee.batch_call;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class ClassTaskResolver {
    @Data
    public static class ClassInfo{
        private List<Task.Config> config = new ArrayList<>();
        private List<Method> maybeKeyMethod = new ArrayList<>();
    }

    private String debugPrefix;

    private Class clazz;

    public ClassTaskResolver(Class clazz){
        this.clazz = clazz;
        this.debugPrefix = "["+clazz.getName()+"] ";
    }
    private Task.Config calcuateConfig(BatchCall annotation, Class clazz, Method method){
        return new MethodTaskResolver(annotation,clazz,method).resolve();
    }

    private List<Task.Config> calcuateMultiplyConfig(MultipleBatchCall annotation, Class clazz, Method method){
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
            log.info("{} fields {}",parentClazz,fields);
            for( Field field :fields ){
                BatchCall batchCall = AnnotationUtils.findAnnotation(field,BatchCall.class);
                MultipleBatchCall multipleBatchCall = AnnotationUtils.findAnnotation(field,MultipleBatchCall.class);
                if( batchCall != null || multipleBatchCall != null ){
                    //注意这里一个字段可能同时匹配了BatchCall与MultipleBatchCall
                    if( batchCall != null ){
                        //找到了一个BatchCall
                        Method method = this.tryGetterMethod(clazz,field);
                        if( method == null ){
                            throw new InvalidAnnotationExcpetion(this.debugPrefix,"Could not found @BachCall "+field.getName()+" getter" );
                        }
                        Method method2 = this.isValidKeyMethod(method);
                        if( method2 == null){
                            throw new InvalidAnnotationExcpetion(this.debugPrefix,"@BachCall "+method.getName()+" is invalid " );
                        }
                        Task.Config config = calcuateConfig(batchCall,clazz,method2);
                        result.config.add(config);
                    }
                    if( multipleBatchCall != null){
                        //找到了一个MultiplyBatchCall
                        Method method = tryGetterMethod(clazz,field);
                        if( method == null ){
                            throw new InvalidAnnotationExcpetion(this.debugPrefix,"Could not found @MultipleBatchCall "+field.getName()+" getter or it is invalid" );
                        }
                        Method method2 = this.isValidKeyMethod(method);
                        if( method2 == null){
                            throw new InvalidAnnotationExcpetion(this.debugPrefix,"@BachCall "+method.getName()+" is invalid " );
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
            }
            parentClazz = parentClazz.getSuperclass();
        }
    }

    private void calcuateClassMethodInfo(Class rootClazz, ClassInfo result){
        Class clazz = rootClazz;
        while( clazz != Object.class) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                BatchCall batchCall = AnnotationUtils.findAnnotation(method, BatchCall.class);
                MultipleBatchCall multipleBatchCall = AnnotationUtils.findAnnotation(method, MultipleBatchCall.class);
                if (batchCall != null || multipleBatchCall != null) {
                    if( Modifier.isPublic(method.getModifiers()) == false ){
                        throw new InvalidAnnotationExcpetion(this.debugPrefix,"@BachCall "+ method.getName() + " is not public");
                    }
                    //注意这里一个方法可能同时匹配了BatchCall与MultipleBatchCall
                    if (batchCall != null) {
                        //找到了一个BatchCall
                        Method method2 = isValidKeyMethod(method);
                        if (method2 == null) {
                            throw new InvalidAnnotationExcpetion(this.debugPrefix,"@BachCall "+ method.getName() + " is invalid");
                        }
                        Task.Config config = calcuateConfig(batchCall, rootClazz, method2);
                        result.config.add(config);
                    }
                    if (multipleBatchCall != null) {
                        Method method2 = isValidKeyMethod(method);
                        if (method2 == null) {
                            throw new InvalidAnnotationExcpetion(this.debugPrefix,"@MultipleBatchCall " + method.getName() + " is invalid");
                        }
                        //找到了一个MultiplyBatchCall
                        List<Task.Config> config = calcuateMultiplyConfig(multipleBatchCall, rootClazz, method2);
                        result.config.addAll(config);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    public ClassInfo resolve(){
        ClassInfo result = new ClassInfo();
        //查找Field信息
        this.calcuateClassFieldInfo(clazz,result);

        //查找Method信息
        this.calcuateClassMethodInfo(clazz,result);
        return result;
    }
}
