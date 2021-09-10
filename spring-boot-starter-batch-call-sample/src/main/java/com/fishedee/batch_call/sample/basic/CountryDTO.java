package com.fishedee.batch_call.sample.basic;

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

        public User getUser(){
            User single = new User();
            single.setId(this.userId);
            single.setName(this.name);
            single.setLevel(this.level);
            return single;
        }

        private int insertId;

        public void addFinish(int insertId){
            this.insertId = insertId;
        }
    }

    private List<People> peopleList = new ArrayList<>();
}
