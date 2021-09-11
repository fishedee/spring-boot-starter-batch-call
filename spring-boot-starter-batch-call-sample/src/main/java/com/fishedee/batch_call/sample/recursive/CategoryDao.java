package com.fishedee.batch_call.sample.recursive;

import com.fishedee.batch_call.sample.SqlUtil;
import com.fishedee.batch_call.sample.basic.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Component
public class CategoryDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Category> getBatch(List<Integer> ids){
        return this.jdbcTemplate.query("select * from category where id in "+ SqlUtil.getQuestionSql(ids),
                SqlUtil.getArgumentArray(ids),
                SqlUtil.getTypeArray(ids,Types.INTEGER),
                new BeanPropertyRowMapper<>(Category.class));
    }

    private List<List<Integer>> callBatchArgv = new ArrayList<>();

    public void clearGetByParentCallArgv(){
        callBatchArgv.clear();
    }

    public List<List<Integer>> getGetByParentCallArgv(){
        return callBatchArgv;
    }


    public List<Category> getByParent(List<Integer> parentId){
        this.callBatchArgv.add(parentId);
        return this.jdbcTemplate.query("select * from category where parentId in "+ SqlUtil.getQuestionSql(parentId),
                SqlUtil.getArgumentArray(parentId),
                SqlUtil.getTypeArray(parentId,Types.INTEGER),
                new BeanPropertyRowMapper<>(Category.class));
    }
}
