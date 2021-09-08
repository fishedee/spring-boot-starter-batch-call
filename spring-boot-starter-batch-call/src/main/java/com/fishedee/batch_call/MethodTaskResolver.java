package com.fishedee.batch_call;

import com.fishedee.reflection_boost.GenericActualArgumentExtractor;
import com.fishedee.reflection_boost.GenericFormalArgumentFiller;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

@Slf4j
public class MethodTaskResolver {

    private String debugPrefix;

    private BatchCall annotation;

    private Class clazz;

    private Method getKeyMethod;

    public MethodTaskResolver(BatchCall annotation, Class clazz, Method getKeyMethod){
        this.debugPrefix = "["+clazz.getName()+":"+getKeyMethod.getName()+"] ";
        this.annotation = annotation;
        this.clazz = clazz;
        this.getKeyMethod = getKeyMethod;
    }

    private Type getInvokeMethodArgumentType(Class clazz,Method method){
        Class declaringClass = method.getDeclaringClass();
        GenericActualArgumentExtractor extractor = new GenericActualArgumentExtractor(clazz,declaringClass);
        GenericFormalArgumentFiller filler = new GenericFormalArgumentFiller(extractor);
        Type argumentType = method.getGenericParameterTypes()[0];
        if( argumentType instanceof ParameterizedType == false ){
            throw new InvalidAnnotationExcpetion(this.debugPrefix,method.getName()+" argument type must be generic Type");
        }
        ParameterizedType parameterizedType = (ParameterizedType)argumentType;
        Class rawType = (Class)parameterizedType.getRawType();
        if( rawType != List.class){
            throw new InvalidAnnotationExcpetion(this.debugPrefix,method.getName()+" argument type must be List<xxx> generic Type");
        }
        return filler.fillType(parameterizedType.getActualTypeArguments()[0]);
    }

    private Type getInvokeMethodReturnType(Class clazz,Method method){
        Class declaringClass = method.getDeclaringClass();
        GenericActualArgumentExtractor extractor = new GenericActualArgumentExtractor(clazz,declaringClass);
        GenericFormalArgumentFiller filler = new GenericFormalArgumentFiller(extractor);
        Type returnType = method.getGenericReturnType();
        Type realReturnType = filler.fillType(returnType);
        if( realReturnType == void.class ||
                realReturnType == Void.class ){
            return null;
        }
        if( realReturnType instanceof ParameterizedType == false ){
            throw new InvalidAnnotationExcpetion(this.debugPrefix,method.getName()+" return type must be generic Type");
        }
        ParameterizedType parameterizedType = (ParameterizedType)realReturnType;
        Class rawType = (Class)parameterizedType.getRawType();
        if( rawType != List.class){
            throw new InvalidAnnotationExcpetion(this.debugPrefix,method.getName()+" return type must be List<xxx> generic Type");
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

    private Method getResultKeyMethod(Type t,String keyName){
        keyName = keyName.trim();
        if( keyName.equals("")){
            throw new InvalidAnnotationExcpetion(this.debugPrefix,"resultMatchKey is Empty");
        }
        String keyMethodName = GetterUtil.getGetterMethodName(keyName);
        try{
            Class clazz = this.getClassOfType(t);
            Method method = clazz.getMethod(keyMethodName);
            return method;
        }catch(NoSuchMethodException e ){
            throw new InvalidAnnotationExcpetion(this.debugPrefix,"resultMatchKey "+keyName+" getter method is empty");
        }catch(SecurityException e){
            throw new InvalidAnnotationExcpetion(this.debugPrefix,"resultMatchKey "+keyName+" getter method error");
        }
    }

    private void checkKeyTypeHashValid(Type keyType){
        if( keyType instanceof Class == false ){
            throw new InvalidAnnotationExcpetion(this.debugPrefix,"KeyType ["+ keyType +"] must be not generic type");
        }
        Class keyClass = (Class)keyType;
        try{
            Method hashCodeMethod = keyClass.getMethod("hashCode");
            if( hashCodeMethod.getDeclaringClass() != keyClass){
                throw new InvalidAnnotationExcpetion(this.debugPrefix,"KeyType ["+keyType+"] should define hashCode method ");
            }
            Method equalsMethod = keyClass.getMethod("equals",Object.class);
            if( equalsMethod.getDeclaringClass() != keyClass){
                throw new InvalidAnnotationExcpetion(this.debugPrefix,"KeyType ["+keyType+"] should define equals method ");
            }
        }catch(NoSuchMethodException e ){
            //这句不可能产生的，直接抛出
            throw new RuntimeException(e);
        }catch(SecurityException e){
            //这句不可能产生的，直接抛出
            throw new RuntimeException(e);
        }
    }

    @Data
    public static class CallbackInfo{
        private Method callbackMethod;
        private boolean callbackMethodArgumentIsListType;
    }
    private CallbackInfo getCallbackMethodForMatchKey(Class clazz,String methodName,Type argumentType){
        CallbackInfo result = new CallbackInfo();

        InvalidAnnotationExcpetion firstException = null;
        InvalidAnnotationExcpetion secondeException = null;
        Method firstMethod;
        Method secondMethod;

        try{
            firstMethod = this.getCallbackMethod(clazz,methodName,argumentType);
            result.callbackMethod = firstMethod;
            result.callbackMethodArgumentIsListType = false;
            return result;
        }catch(InvalidAnnotationExcpetion e){
            firstException = e;
        }

        try{
            secondMethod = this.getCallbackMethodListType(clazz,methodName,argumentType);
            result.callbackMethod = secondMethod;
            result.callbackMethodArgumentIsListType = true;
            return result;
        }catch(InvalidAnnotationExcpetion e){
            secondeException = e;
        }

        String message = "Could not found callback method\n"+firstException.getMessage()+"\n"+secondeException.getMessage();
        throw new InvalidAnnotationExcpetion(this.debugPrefix,message);
    }

    private CallbackInfo getCallbackMethodForSequenceKey(Class clazz,String methodName,Type argumentType){
        CallbackInfo result = new CallbackInfo();
        result.callbackMethod =  this.getCallbackMethod(clazz,methodName,argumentType);
        result.callbackMethodArgumentIsListType = false;
        return result;
    }
    private Method getCallbackMethodListType(Class clazz,String methodName,Type argumentType){
        try{
            Method method = clazz.getMethod(methodName,List.class);
            Type methodArgmentType = method.getGenericParameterTypes()[0];
            Class methodDeclaringClass = method.getDeclaringClass();
            GenericActualArgumentExtractor extractor = new GenericActualArgumentExtractor(clazz,methodDeclaringClass);
            GenericFormalArgumentFiller filler = new GenericFormalArgumentFiller(extractor);
            ParameterizedType methodGenericArgmentType = (ParameterizedType)filler.fillType(methodArgmentType);
            Type actualMethodArgumentType = methodGenericArgmentType.getActualTypeArguments()[0];
            if( actualMethodArgumentType.getTypeName().equals(argumentType.getTypeName()) == false ){
                throw new InvalidAnnotationExcpetion(this.debugPrefix,"methodArgmentType "+actualMethodArgumentType.getTypeName()+" is not equal to invokeMethodReturnType "+argumentType.getTypeName());
            }
            return method;
        }catch(NoSuchMethodException e){
            throw new InvalidAnnotationExcpetion(this.debugPrefix,"No Such Method "+clazz+" -> "+methodName+"(List<"+argumentType.getTypeName()+">)");
        }
    }

    private Method getCallbackVoidMethod(Class clazz,String methodName){
        try{
            Method method = clazz.getMethod(methodName);
            return method;
        }catch(NoSuchMethodException e){
            throw new InvalidAnnotationExcpetion(this.debugPrefix,"Could not found "+clazz+" -> "+methodName+"()");
        }
    }


    private Method getCallbackMethod(Class clazz,String methodName,Type argumentType){
        try{
            Class argumentClass = this.getClassOfType(argumentType);
            Method method = clazz.getMethod(methodName,argumentClass);
            Type methodArgmentType = method.getGenericParameterTypes()[0];
            Class methodDeclaringClass = method.getDeclaringClass();
            GenericActualArgumentExtractor extractor = new GenericActualArgumentExtractor(clazz,methodDeclaringClass);
            GenericFormalArgumentFiller filler = new GenericFormalArgumentFiller(extractor);
            methodArgmentType = filler.fillType(methodArgmentType);
            if( methodArgmentType.getTypeName().equals(argumentType.getTypeName()) == false ){
                throw new InvalidAnnotationExcpetion(this.debugPrefix,"methodArgmentType "+methodArgmentType.getTypeName()+" is not equal to invokeMethodReturnType "+argumentType.getTypeName());
            }
            return method;
        }catch(NoSuchMethodException e){
            throw new InvalidAnnotationExcpetion(this.debugPrefix,"No Such Method "+clazz+" -> "+methodName+"("+argumentType.getTypeName()+")");
        }
    }

    public Task.Config resolve(){
        try{
            ResultMatch resultMatch = annotation.resultMatch();
            Type keyType = getKeyMethod.getGenericReturnType();
            Class invokeTarget = annotation.invokeTarget();
            Method invokeMethod = invokeTarget.getMethod(annotation.invokeMethod(),List.class);

            //检查批量调用的参数类型，与Key类型是否匹配
            Type invokeArgumentType = getInvokeMethodArgumentType(invokeTarget,invokeMethod);
            if( invokeArgumentType.getTypeName().equals(keyType.getTypeName()) == false ){
                throw new InvalidAnnotationExcpetion(this.debugPrefix,"does not satisfy invoke argument type,key type is "+keyType.getTypeName()+" and invokeMethod argument type is "+invokeArgumentType.getTypeName());
            }

            //检查批量调用的返回值参数类型，与结果回调参数类型是否匹配
            boolean callbackMethodArgumentIsEmpty = false;
            CallbackInfo callbackInfo;
            Type invokeReturnType = getInvokeMethodReturnType(invokeTarget,invokeMethod);
            if( invokeReturnType != null ){
                //invokeMethod的返回值为非Void类型
                callbackMethodArgumentIsEmpty = false;
                if( resultMatch == ResultMatch.KEY ){
                    callbackInfo = getCallbackMethodForMatchKey(clazz,annotation.callbackMethod(),invokeReturnType);
                }else{
                    callbackInfo = getCallbackMethodForSequenceKey(clazz,annotation.callbackMethod(),invokeReturnType);
                }
            }else{
                //invokeMethod的返回值为Void类型
                callbackMethodArgumentIsEmpty = true;
                if( resultMatch == ResultMatch.KEY ){
                    throw new InvalidAnnotationExcpetion(this.debugPrefix,"call back method return type is Void.class，so do not support ResultMatch.KEY");
                }
                callbackInfo = new CallbackInfo();
                callbackInfo.callbackMethod = this.getCallbackVoidMethod(clazz,annotation.callbackMethod());
                callbackInfo.callbackMethodArgumentIsListType = false;
            }


            //检查key类型是否重写了equals与hashCode，并获取resultKeyMethod
            Method resultKeyMethod = null;
            if( resultMatch == ResultMatch.KEY ){
                checkKeyTypeHashValid(keyType);
                resultKeyMethod = this.getResultKeyMethod(invokeReturnType,annotation.resultMatchKey());
                Class resultKeyType = resultKeyMethod.getReturnType();
                if( resultKeyType.getTypeName().equals(keyType.getTypeName()) == false){
                    throw new InvalidAnnotationExcpetion(this.debugPrefix,resultKeyMethod.getName()+" return type is not equal "+keyType.getTypeName());
                }
            }

            Task.Config result = new Task.Config();
            result.setBatchCall(annotation);
            result.setClazz(clazz);
            result.setGetKeyMethod(getKeyMethod);
            result.setCallbackMethod(callbackInfo.getCallbackMethod());
            result.setCallbackMethodArgumentIsListType(callbackInfo.isCallbackMethodArgumentIsListType());
            result.setCallbackMethodArgumentIsEmpty(callbackMethodArgumentIsEmpty);
            result.setInvokeTarget(invokeTarget);
            result.setInvokeMethod(invokeMethod);
            result.setResultKeyMethod(resultKeyMethod);
            return result;
        }catch(NoSuchMethodException e){
            throw new InvalidAnnotationExcpetion(this.debugPrefix,"Chould not found method",e);
        }
    }
}
