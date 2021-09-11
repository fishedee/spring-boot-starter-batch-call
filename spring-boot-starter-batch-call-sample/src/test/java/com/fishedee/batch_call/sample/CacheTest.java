package com.fishedee.batch_call.sample;

import com.fishedee.batch_call.BatchCallTask;
import com.fishedee.batch_call.JsonAssertUtil;
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

        JsonAssertUtil.checkEqualStrict("[[10001],[10002],[10003]]",userDao.getGetBatch2CallArgv());



        JsonAssertUtil.checkEqualStrict("{\"decription\":null,\"userId\":10001,\"name\":\"fish\",\"level\":12,\"child\":" +
                "{\"decription\":null,\"userId\":10002,\"name\":\"cat\",\"level\":34,\"child\":" +
                "{\"decription\":null,\"userId\":10003,\"name\":\"dog\",\"level\":56,\"child\":null}" +
                "}" +
                "}\n",people2);
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

        JsonAssertUtil.checkEqualStrict("[[10001]]",userDao.getGetBatch2CallArgv());


        JsonAssertUtil.checkEqualStrict("{\"decription\":null,\"userId\":10001,\"name\":\"fish\",\"level\":12,\"child\":" +
                "{\"decription\":null,\"userId\":10002,\"name\":\"cat\",\"level\":34,\"child\":" +
                "{\"decription\":null,\"userId\":10003,\"name\":\"dog\",\"level\":56,\"child\":null}" +
                "}" +
                "}\n",people2);
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

        JsonAssertUtil.checkEqualStrict("[[10001,10005,10006],[10005,10006],[10006]]",userDao.getGetBatch2CallArgv());


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

        JsonAssertUtil.checkEqualStrict("[[10001,10005,10006]]",userDao.getGetBatch2CallArgv());


        JsonAssertUtil.checkEqualStrict("[{\"decription\":null,\"userId\":10001,\"name\":\"fish\",\"level\":12,\"child\":" +
                "{\"decription\":null,\"userId\":10005,\"name\":null,\"level\":0,\"child\":" +
                    "{\"decription\":null,\"userId\":10006,\"name\":null,\"level\":0,\"child\":null}" +
                "}" +
                "}," +
                "{\"decription\":null,\"userId\":10005,\"name\":null,\"level\":0,\"child\":" +
                    "{\"decription\":null,\"userId\":10006,\"name\":null,\"level\":0,\"child\":null}}," +
                "{\"decription\":null,\"userId\":10006,\"name\":null,\"level\":0,\"child\":null}]\n",listPeople);
    }
}
