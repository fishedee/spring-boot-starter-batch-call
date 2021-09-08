package com.fishedee.batch_call.sample.generic;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public class DataGenericDao<T1,T2> {

    Class entityClass;
    public DataGenericDao(){
        ParameterizedType t = (ParameterizedType)this.getClass().getGenericSuperclass();
        entityClass = (Class)t.getActualTypeArguments()[1];
    }

    public void injectRandomData(int index,Object data)throws Exception{
        Method[] methods = data.getClass().getMethods();
        for( Method method :methods ){
            Class[] parameterClass = method.getParameterTypes();
            Class returnClass = method.getReturnType();
            if( parameterClass.length == 1 && returnClass == void.class){
                Class firstParameterClazz = parameterClass[0];
                if( firstParameterClazz == Integer.class ||
                    firstParameterClazz == int.class){
                    method.invoke(data,index);
                }else if( firstParameterClazz == String.class){
                    method.invoke(data,method.getName()+"_"+index);
                }
            }
        }
    }

    public List<T2> getBatch(List<T1> data){
        List<T2> result = new ArrayList<>();
        for( int i = 0 ;i != data.size();i++){
            try{
                Object target = entityClass.newInstance();
                this.injectRandomData(i+1,target);
                result.add((T2)target);
            }catch(Exception e ){
                throw new RuntimeException(e);
            }
        }
        return result;
    }
}
