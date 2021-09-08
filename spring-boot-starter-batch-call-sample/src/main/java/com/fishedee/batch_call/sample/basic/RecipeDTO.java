package com.fishedee.batch_call.sample.basic;

import com.fishedee.batch_call.BatchCall;
import com.fishedee.batch_call.ResultMatch;
import com.fishedee.batch_call.sample.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
public class RecipeDTO {
    @Data
    @EqualsAndHashCode
    public static class Step{

        private String decription;

        @BatchCall(
                task="getUser",
                invokeTarget = UserDao.class,
                invokeMethod = "getBatch",
                callbackMethod = "setUser",
                resultMatch = ResultMatch.KEY,
                resultMatchKey = "id"
        )
        private Integer userId;

        private String name;

        private int level;

        public void setUser(User user){
            this.name = user.getName();
            this.level = user.getLevel();
        }
    }

    private List<Step> stepList = new ArrayList<>();
}
