package com.fishedee.batch_call.sample.basic;

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

        private Integer userId;

        private String name;

        private int level;

        public void setUser(User user){
            this.name = user.getName();
            this.level = user.getLevel();
        }

        public void setUser2(User user){
            if( user!= null){
                this.name = user.getName();
                this.level = user.getLevel();
            }
        }
    }

    private List<Step> stepList = new ArrayList<>();
}
