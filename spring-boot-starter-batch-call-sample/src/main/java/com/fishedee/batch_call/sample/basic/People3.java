package com.fishedee.batch_call.sample.basic;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
public class People3 {

    private String decription;

    private Integer userId;

    private String name;

    private int level;

    private List<People3> children;

    public People3(Integer userId){
        this.userId = userId;
    }

    public List<People3> setUserRecursive(User user){
        this.name = user.getName();
        this.level = user.getLevel();
        if( user.getId() == 10001 ){
            children = Arrays.asList(
                new People3(10002),
                new People3(10003),
                new People3(10002),
                new People3(10003)
            );
        }else{
            children = null;
        }
        return children;
    }
}
