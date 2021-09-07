package com.fishedee.batch_call.sample;

import com.fishedee.batch_call.BatchCall;
import com.fishedee.batch_call.MultipleBatchCall;
import com.fishedee.batch_call.ResultMatch;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CountryDTO {
    @Data
    public static class Step{
        @BatchCall(
                task="addUser",
                invokeTarget = UserDao.class,
                invokeMethod = "insertBatch",
                callbackMethod = "addFinish"
        )
        private Integer userId;

        private String name;

        private int level;

        private void addFinish(){

        }
    }

    private List<User> userList = new ArrayList<>();
}
