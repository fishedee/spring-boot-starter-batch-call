package com.fishedee.batch_call.sample.basic;

import com.fishedee.batch_call.sample.SqlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;
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

    public List<User> getAll(){
        return this.jdbcTemplate.query("select * from user",
                new Object[]{},
                new int[]{},
                new BeanPropertyRowMapper<>(User.class));
    }
}
