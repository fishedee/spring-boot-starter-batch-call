package com.fishedee.batch_call.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    //从id转换为name
    public List<User> getBatch(List<Integer> userIds){
        return this.jdbcTemplate.query("select * from user where id in (?)",
                new Object[]{userIds},
                new int[]{Types.ARRAY},
                new BeanPropertyRowMapper<>(User.class));
    }
}
