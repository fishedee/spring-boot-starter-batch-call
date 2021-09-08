package com.fishedee.batch_call.sample.basic;

import com.fishedee.batch_call.BatchCall;
import com.fishedee.batch_call.sample.User;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CountryDTO {
    @Data
    public static class People{

        private Integer userId;

        private String name;

        private int level;

        @BatchCall(
                task="addUser",
                invokeTarget = UserDao.class,
                invokeMethod = "insertBatch",
                callbackMethod = "addFinish"
        )
        public User getUser(){
            User single = new User();
            single.setId(this.userId);
            single.setName(this.name);
            single.setLevel(this.level);
            return single;
        }

        public void addFinish(){

        }
    }

    private List<People> peopleList = new ArrayList<>();
}
