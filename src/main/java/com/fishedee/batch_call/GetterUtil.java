package com.fishedee.batch_call;

public class GetterUtil {
    public static String getGetterMethodName(String fieldName){
        if( fieldName == null || fieldName.equals("")){
            return "";
        }
        if( fieldName.length() > 2 ){
            //eBlog的名字，转换为getter为geteBlog
            String second = fieldName.substring(1,2);
            if( second.equals(second.toUpperCase())){
                return "get"+fieldName;
            }
        }

        //其他情况是，name，转换为getName
        String first = fieldName.substring(0,1).toUpperCase();
        String rest = fieldName.substring(1);
        return "get"+first+rest;
    }
}
