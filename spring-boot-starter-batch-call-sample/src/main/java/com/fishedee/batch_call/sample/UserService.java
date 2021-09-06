package com.fishedee.batch_call.sample;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserService {

    //从id转换为name
    public List<String> getBatchUser(List<Long> userIds){
        Map<Long,String> map = new HashMap<>();
        map.put(10001L,"fish");
        map.put(10002L,"cat");
        map.put(10003L,"dog");

        List<String> result = new ArrayList<>();
        for( int i = 0 ;i != userIds.size();i++){
            result.add(map.get(userIds));
        }
        return result;
    }

    //从id转换为name
    public List<Integer> getBatchUserLevel(List<Long> userIds){
        Map<Long,Integer> map = new HashMap<>();
        map.put(10001L,123);
        map.put(10002L,456);
        map.put(10003L,789);

        List<Integer> result = new ArrayList<>();
        for( int i = 0 ;i != userIds.size();i++){
            result.add(map.get(userIds));
        }
        return result;
    }
}
