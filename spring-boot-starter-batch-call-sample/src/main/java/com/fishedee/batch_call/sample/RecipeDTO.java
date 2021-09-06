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
                task="getUserName",
                invokeTarget = UserService.class,
                invokeMethod = "getBatchUser",
                callbackMethod = "setUserName"
        )
        private Long userId;

        private String name;

        public void setUserName(String name){
            this.name = name;
        }
    }
    private List<Step> stepList = new ArrayList<>();

    @Data
    public static class Step2{

        private String decription;

        @MultipleBatchCall(
                {
                        @BatchCall(
                                task="getUserName",
                                invokeTarget = UserService.class,
                                invokeMethod = "getBatchUser",
                                callbackMethod = "setUserName"
                        ),
                        @BatchCall(
                                task="getUserLevel",
                                invokeTarget = UserService.class,
                                invokeMethod = "getBatchUserLevel",
                                callbackMethod = "setUserLevel"
                        )
                }
        )
        private Long userId;

        private String name;

        private int level;

        public void setUserName(String name){
            this.name = name;
        }

        public void setUserLevel(int level){this.level = level;}
    }
    private List<Step2> step2List = new ArrayList<>();
}
