package com.fishedee.batch_call.sample.basic;

import com.fishedee.batch_call.sample.SqlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class UserDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    //从id转换为name
    public List<User> getBatch(List<Integer> userIds){
        return this.jdbcTemplate.query("select * from user where id in "+ SqlUtil.getQuestionSql(userIds),
                SqlUtil.getArgumentArray(userIds),
                SqlUtil.getTypeArray(userIds,Types.INTEGER),
                new BeanPropertyRowMapper<>(User.class));
    }

    //从id转换为name
    public void insertBatch(List<User> users){
        for(User user :users){
            this.jdbcTemplate.update("insert into user(id,name,level) values(?,?,?)",user.getId(),user.getName(),user.getLevel());
        }
    }

    //从id转换为name
    public List<Integer> insertBatch2(List<User> users){
        List<Integer> result = new ArrayList<>();
        int id = 30001;
        for(User user :users){
            result.add(id);
            this.jdbcTemplate.update("insert into user(id,name,level) values(?,?,?)",id,user.getName(),user.getLevel());
            id++;
        }
        return result;
    }

    //从id转换为name
    public List<Integer> insertBatch3(List<User> users){
        return this.insertBatch2(users.subList(0,1));
    }

    private List<List<Integer>> callBatchArgv = new ArrayList<>();

    public void clearGetBatch2CallArgv(){
        callBatchArgv.clear();
    }

    public List<List<Integer>> getGetBatch2CallArgv(){
        return callBatchArgv;
    }

    public List<User> getBatch2(List<Integer> userIds){
        List<Integer> newArgv = new ArrayList<>();
        newArgv.addAll(userIds);
        callBatchArgv.add(newArgv);

        //故意取了全部数据
        return this.jdbcTemplate.query("select * from user",
                new Object[]{},
                new int[]{},
                new BeanPropertyRowMapper<>(User.class));
    }

    public List<User> getAll(){
        return this.jdbcTemplate.query("select * from user",
                new Object[]{},
                new int[]{},
                new BeanPropertyRowMapper<>(User.class));
    }
}
