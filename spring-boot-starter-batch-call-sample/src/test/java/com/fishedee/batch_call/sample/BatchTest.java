package com.fishedee.batch_call.sample;

import com.fishedee.batch_call.BatchCallTask;
import com.fishedee.batch_call.JsonAssertUtil;
import com.fishedee.batch_call.ResultMatchByKey;
import com.fishedee.batch_call.autoconfig.BatchCallAutoConfiguration;
import com.fishedee.batch_call.sample.basic.People2;
import com.fishedee.batch_call.sample.basic.People3;
import com.fishedee.batch_call.sample.basic.User;
import com.fishedee.batch_call.sample.basic.UserDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

@DataJpaTest(includeFilters = @ComponentScan.Filter(
        type= FilterType.ASSIGNABLE_TYPE,
        classes = {UserDao.class}
))
@Import(BatchCallAutoConfiguration.class)
public class BatchTest {

    @Autowired
    private UserDao userDao;

    @Test
    public void testDefaultBatch(){
        List<People3> peopleList = Arrays.asList(
                new People3(10001),
                new People3(10001)
        );

        userDao.clearGetBatch2CallArgv();;

        new BatchCallTask()
                .collectKey(People3.class,People3::getUserId)
                .call(userDao,UserDao::getBatch2,new ResultMatchByKey<>(User::getId))
                .dispatch(People3::setUserRecursive)
                .run(peopleList);

        JsonAssertUtil.checkEqualStrict("[" +
                "[10001,10001],"+
                "[10002,10003,10002,10003,10002,10003,10002,10003]"+
                "]",userDao.getGetBatch2CallArgv());
    }

    @Test
    public void testDefineBatch(){
        List<People3> peopleList = Arrays.asList(
                new People3(10001),
                new People3(10001),
                new People3(10001),
                new People3(10001)
        );

        userDao.clearGetBatch2CallArgv();;

        new BatchCallTask()
                .collectKey(People3.class,People3::getUserId)
                .call(userDao,UserDao::getBatch2,new ResultMatchByKey<>(User::getId))
                .dispatch(People3::setUserRecursive)
                .setBatchSize(3)
                .run(peopleList);

        JsonAssertUtil.checkEqualStrict("[" +
                "[10001,10001,10001],"+
                "[10001,10002,10003],"+
                "[10002,10003,10002],"+
                "[10003,10002,10003],"+
                "[10002,10003,10002],"+
                "[10003,10002,10003],"+
                "[10002,10003]"+
                "]",userDao.getGetBatch2CallArgv());
    }
}
