package com.fishedee.batch_call;

import com.fishedee.reflection_boost.GenericActualArgumentExtractor;
import com.fishedee.reflection_boost.GenericFormalArgumentFiller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class TaskChecker {

    private Type getInvokeMethodArgumentType(Class clazz,Method method){
        Class declaringClass = method.getDeclaringClass();
        GenericActualArgumentExtractor extractor = new GenericActualArgumentExtractor(clazz,declaringClass);
        GenericFormalArgumentFiller filler = new GenericFormalArgumentFiller(extractor);
        Type argumentType = method.getGenericParameterTypes()[0];
        if( argumentType instanceof ParameterizedType == false ){
            throw new InvalidAnnotationExcpetion(method.getName()+" argument type must be generic Type");
        }
        ParameterizedType parameterizedType = (ParameterizedType)argumentType;
        Class rawType = (Class)parameterizedType.getRawType();
        if( rawType != List.class){
            throw new InvalidAnnotationExcpetion(method.getName()+" argument type must be List<xxx> generic Type");
        }
        return parameterizedType.getActualTypeArguments()[0];
    }
    private Type getInvokeMethodReturnType(Class clazz,Method method){
        Class declaringClass = method.getDeclaringClass();
        GenericActualArgumentExtractor extractor = new GenericActualArgumentExtractor(clazz,declaringClass);
        GenericFormalArgumentFiller filler = new GenericFormalArgumentFiller(extractor);
        Type returnType = method.getGenericReturnType();
        Type realReturnType = filler.fillType(returnType);
        if( realReturnType instanceof ParameterizedType == false ){
            throw new InvalidAnnotationExcpetion(method.getName()+" return type must be generic Type");
        }
        ParameterizedType parameterizedType = (ParameterizedType)realReturnType;
        Class rawType = (Class)parameterizedType.getRawType();
        if( rawType != List.class){
            throw new InvalidAnnotationExcpetion(method.getName()+" return type must be List<xxx> generic Type");
        }
        return parameterizedType.getActualTypeArguments()[0];
    }

    private Class getClassOfType(Type type){
        if( type instanceof Class){
            return (Class)type;
        }
        ParameterizedType parameterizedType = (ParameterizedType) type;
        return (Class)parameterizedType.getRawType();
    }

    private Method getResultKeyMethod(Class clazz,String keyName){
        keyName = keyName.trim();
        if( keyName.equals("")){
            throw new InvalidAnnotationExcpetion("resultMatchKey is Empty");
        }
        String keyMethodName = GetterUtil.getGetterMethodName(keyName);
        try{
            Method method = clazz.getMethod(keyMethodName);
            return method;
        }catch(NoSuchMethodException e ){
            throw new InvalidAnnotationExcpetion("resultMatchKey "+keyName+" getter method is empty");
        }catch(SecurityException e){
            throw new InvalidAnnotationExcpetion("resultMatchKey "+keyName+" getter method error");
        }
    }

    private void checkKeyTypeHashValid(Type keyType){
        if( keyType instanceof Class == false ){
            throw new InvalidAnnotationExcpetion("KeyType ["+ keyType +"] must be not generic type");
        }
        Class keyClass = (Class)keyType;
        try{
            Method hashCodeMethod = keyClass.getMethod("hashCode");
            if( hashCodeMethod.getDeclaringClass() != keyClass){
                throw new InvalidAnnotationExcpetion("KeyType ["+keyType+"] should define hashCode method ");
            }
            Method equalsMethod = keyClass.getMethod("equals",Object.class);
            if( equalsMethod.getDeclaringClass() != keyClass){
                throw new InvalidAnnotationExcpetion("KeyType ["+keyType+"] should define equals method ");
            }
        }catch(NoSuchMethodException e ){
            //这句不可能产生的，直接抛出
            throw new RuntimeException(e);
        }catch(SecurityException e){
            //这句不可能产生的，直接抛出
            throw new RuntimeException(e);
        }
    }

    public Task.Config calcuateConfig(BatchCall annotation, Class clazz, Method getKeyMethod){
        try{
            ResultMatch resultMatch = annotation.resultMatch();
            Type keyType = getKeyMethod.getGenericReturnType();
            Class invokeTarget = annotation.invokeTarget();
            Method invokeMethod = invokeTarget.getMethod(annotation.invokeMethod(),List.class);

            //检查批量调用的参数类型，与Key类型是否匹配
            Type invokeArgumentType = getInvokeMethodArgumentType(invokeTarget,invokeMethod);
            if( invokeArgumentType.getTypeName().equals(keyType.getTypeName()) == false ){
                throw new InvalidAnnotationExcpetion("does not satisfy invoke argument type,key type is "+keyType.getTypeName()+" and invokeMethod argument type is "+invokeArgumentType.getTypeName());
            }

            //检查批量调用的返回值参数类型，与结果回调参数类型是否匹配
            Type invokeReturnType = getInvokeMethodReturnType(invokeTarget,invokeMethod);
            Class invokeReturnTypeClass = this.getClassOfType(invokeReturnType);
            Method callbackMethod = clazz.getMethod(annotation.callbackMethod(),invokeReturnTypeClass);


            //检查key类型是否重写了equals与hashCode，并获取resultKeyMethod
            Method resultKeyMethod = null;
            if( resultMatch == ResultMatch.KEY ){
                checkKeyTypeHashValid(keyType);
                resultKeyMethod = this.getResultKeyMethod(invokeReturnTypeClass,annotation.resultMatchKey());
                Class resultKeyType = resultKeyMethod.getReturnType();
                if( resultKeyType.getTypeName().equals(keyType.getTypeName()) == false){
                    throw new InvalidAnnotationExcpetion(resultKeyMethod.getName()+" return type is not equal "+keyType.getTypeName());
                }
            }

            Task.Config result = new Task.Config();
            result.setBatchCall(annotation);
            result.setClazz(clazz);
            result.setGetKeyMethod(getKeyMethod);
            result.setCallbackMethod(callbackMethod);
            result.setInvokeTarget(invokeTarget);
            result.setInvokeMethod(invokeMethod);
            result.setResultKeyMethod(resultKeyMethod);
            return result;
        }catch(NoSuchMethodException e){
            throw new InvalidAnnotationExcpetion("Chould not found method",e);
        }
    }

    public void check(List<Task> taskList){

    }
}
