package com.fishedee.batch_call.sample.recursive;

import com.fishedee.batch_call.sample.SqlUtil;
import com.fishedee.batch_call.sample.basic.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.List;

@Component
public class CategoryDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Category> getByParent(List<Integer> parentId){
        return this.jdbcTemplate.query("select * from category where parentId in "+ SqlUtil.getQuestionSql(parentId),
                SqlUtil.getArgumentArray(parentId),
                SqlUtil.getTypeArray(parentId,Types.INTEGER),
                new BeanPropertyRowMapper<>(Category.class));
    }
}
