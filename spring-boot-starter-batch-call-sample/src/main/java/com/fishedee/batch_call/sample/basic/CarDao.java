package com.fishedee.batch_call.sample.basic;

import com.fishedee.batch_call.sample.SqlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.List;

@Component
public class CarDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    //从id转换为name
    public List<Car> getByDriverId(List<Integer> driverIds){
        return this.jdbcTemplate.query("select * from car where driverId in "+ SqlUtil.getQuestionSql(driverIds),
                SqlUtil.getArgumentArray(driverIds),
                SqlUtil.getTypeArray(driverIds, Types.INTEGER),
                new BeanPropertyRowMapper<>(Car.class));
    }
}
