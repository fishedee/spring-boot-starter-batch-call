package com.fishedee.batch_call.sample;

import com.fishedee.batch_call.BatchCall;
import com.fishedee.batch_call.MultipleBatchCall;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RecipeDTO {
    @Data
    public static class Step{

        private String decription;

        @BatchCall(
                task="getUser",
                invokeTarget = UserDao.class,
                invokeMethod = "getBatch",
                callbackMethod = "setUser"
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
