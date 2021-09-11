package com.fishedee.batch_call.sample;

import com.fishedee.batch_call.BatchCallTask;
import com.fishedee.batch_call.ResultMatchByKey;
import com.fishedee.batch_call.autoconfig.BatchCallAutoConfiguration;
import com.fishedee.batch_call.sample.basic.People2;
import com.fishedee.batch_call.sample.basic.User;
import com.fishedee.batch_call.sample.basic.UserDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(includeFilters = @ComponentScan.Filter(
        type= FilterType.ASSIGNABLE_TYPE,
        classes = {UserDao.class}
))
@Import(BatchCallAutoConfiguration.class)
public class CacheTest {
    //FIXME返回数据assert
    @Autowired
    private UserDao userDao;

    @Test
    public void testNoCache(){
        People2 people2 = new People2();
        people2.setUserId(10001);

        userDao.clearGetBatch2CallArgv();

        new BatchCallTask()
                .collectKey(People2.class,People2::getUserId)
                .call(userDao,UserDao::getBatch2,new ResultMatchByKey<>(User::getId))
                .dispatch(People2::setUserRecursive)
                .run(people2);

        assertEquals(3,userDao.getGetBatch2CallArgv().size());

    }

    @Test
    public void testCache(){
        People2 people2 = new People2();
        people2.setUserId(10001);

        userDao.clearGetBatch2CallArgv();

        new BatchCallTask()
                .collectKey(People2.class,People2::getUserId)
                .call(userDao,UserDao::getBatch2,new ResultMatchByKey<>(User::getId))
                .dispatch(People2::setUserRecursive)
                .setCacheEnabled(true)
                .run(people2);

        assertEquals(1,userDao.getGetBatch2CallArgv().size());
    }

    @Test
    public void testNoCacheGroupEmpty(){
        People2 people = new People2();
        people.setUserId(10001);

        People2 people2 = new People2();
        people2.setUserId(10005);

        People2 people3 = new People2();
        people3.setUserId(10006);

        List<People2> listPeople = Arrays.asList(people,people2,people3);

        userDao.clearGetBatch2CallArgv();

        new BatchCallTask()
                .collectKey(People2.class,People2::getUserId)
                .call(userDao,UserDao::getBatch2,new ResultMatchByKey<>(User::getId))
                .groupThenDispatch(People2::setUserRecursive2)
                .run(listPeople);

        assertEquals(3,userDao.getGetBatch2CallArgv().size());
    }

    @Test
    public void testNoCacheGroupNotEmpty(){
        People2 people = new People2();
        people.setUserId(10001);

        People2 people2 = new People2();
        people2.setUserId(10005);

        People2 people3 = new People2();
        people3.setUserId(10006);

        List<People2> listPeople = Arrays.asList(people,people2,people3);

        userDao.clearGetBatch2CallArgv();

        new BatchCallTask()
                .collectKey(People2.class,People2::getUserId)
                .call(userDao,UserDao::getBatch2,new ResultMatchByKey<>(User::getId))
                .groupThenDispatch(People2::setUserRecursive2)
                .setCacheEnabled(true)
                .run(listPeople);

        assertEquals(1,userDao.getGetBatch2CallArgv().size());
    }
}
