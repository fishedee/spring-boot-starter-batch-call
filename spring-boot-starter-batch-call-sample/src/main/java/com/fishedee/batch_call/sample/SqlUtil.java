package com.fishedee.batch_call.sample;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SqlUtil {

    public static String getQuestionSql(List<?> data){
        List<String> strings = new ArrayList<>();
        for( int i = 0 ;i != data.size();i++){
            strings.add("?");
        }
        return "("+String.join(",",strings)+")";
    }

    public static Object[] getArgumentArray(List<?> data){
        return data.toArray(new Object[]{});
    }

    public static int[] getTypeArray(List<?> data,int type){
        int[] result = new int[data.size()];
        for( int i = 0 ;i != data.size();i++){
            result[i] = type;
        }
        return result;
    }

    public static String getPrefixQuestionSql(List<?> data){
        List<String> strings = new ArrayList<>();
        for( int i = 0 ;i != data.size();i++){
            strings.add("path like ?");
        }
        return String.join(" or ",strings);
    }

    public static Object[] getPrefixArgumentArray(List<String> data){
        return data.stream().map((single)->single+'%').toArray();
    }
}
