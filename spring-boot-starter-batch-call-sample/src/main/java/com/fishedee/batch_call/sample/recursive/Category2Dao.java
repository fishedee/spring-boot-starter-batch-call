package com.fishedee.batch_call.sample.recursive;

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
public class Category2Dao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Category2> getBatchByPrefix(List<String> paths){
        return this.jdbcTemplate.query("select * from category2 where  "+ SqlUtil.getPrefixQuestionSql(paths),
                SqlUtil.getPrefixArgumentArray(paths),
                SqlUtil.getTypeArray(paths, Types.NVARCHAR),
                new BeanPropertyRowMapper<>(Category2.class));
    }
}
