package com.fishedee.batch_call.sample.basic;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
public class People2 {

    private String decription;

    private Integer userId;

    private String name;

    private int level;

    private People2 child;

    public People2 setUserRecursive(User user){
        this.name = user.getName();
        this.level = user.getLevel();
        if( user.getId() == 10001 ){
            child = new People2();
            child.setUserId(10002);
            return child;
        }else if( user.getId() == 10002 ){
            child = new People2();
            child.setUserId(10003);
            return child;
        }else{
            return null;
        }
    }

    public People2 setUserRecursive2(List<User> users) {
        if( users.size() != 0 ){
            User user = users.get(0);
            this.name = user.getName();
            this.level = user.getLevel();
        }
        if( this.getUserId() == 10001 ){
            child = new People2();
            child.setUserId(10005);
            return child;
        }else if( this.getUserId() == 10005 ){
            child = new People2();
            child.setUserId(10006);
            return child;
        }else{
            return null;
        }
    }
}
