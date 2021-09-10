package com.fishedee.batch_call.sample;

import com.fishedee.batch_call.BatchCallTask;
import com.fishedee.batch_call.CallResultSizeNotEqualException;
import com.fishedee.batch_call.autoconfig.BatchCallAutoConfiguration;
import com.fishedee.batch_call.sample.basic.CountryDTO;
import com.fishedee.batch_call.sample.basic.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest(includeFilters = @ComponentScan.Filter(
        type= FilterType.ASSIGNABLE_TYPE,
        classes = {UserDao.class}
))
@Import(BatchCallAutoConfiguration.class)
public class SequenceMatchTest {

    @Autowired
    private UserDao userDao;

    private CountryDTO countryDTO;

    @BeforeEach
    public void setUp(){
        //初始化数据
        CountryDTO.People people = new CountryDTO.People();
        people.setUserId(10004);
        people.setName("a");
        people.setLevel(12);
        CountryDTO.People people2 = new CountryDTO.People();
        people2.setUserId(10005);
        people2.setName("b");
        people2.setLevel(34);
        CountryDTO.People people3 = new CountryDTO.People();
        people3.setUserId(10006);
        people3.setName("c");
        people3.setLevel(56);


        countryDTO = new CountryDTO();
        countryDTO.getPeopleList().add(people);
        countryDTO.getPeopleList().add(people2);
        countryDTO.getPeopleList().add(people3);
    }

    @Test
    public void sequeceMatchVoidReturnAndNoDispatch(){
        int oldSize = userDao.getAll().size();
        new BatchCallTask()
                .collectKey(CountryDTO.People.class,CountryDTO.People::getUser)
                .call(userDao,UserDao::insertBatch)
                .noDispatch()
                .run(countryDTO);
        int newSize = userDao.getAll().size();
        assertEquals(3,newSize-oldSize);
    }

    @Test
    public void sequeceMatchVoidReturnAndDispatch(){
        int oldSize = userDao.getAll().size();
        List<CountryDTO.People> insertPeopleList = new ArrayList<>();
        new BatchCallTask()
                .collectKey(CountryDTO.People.class,CountryDTO.People::getUser)
                .call(userDao,UserDao::insertBatch)
                .dispatch((CountryDTO.People people)->{
                    insertPeopleList.add(people);
                })
                .run(countryDTO);
        int newSize = userDao.getAll().size();
        assertEquals(3,newSize-oldSize);
        assertIterableEquals(countryDTO.getPeopleList(),insertPeopleList);
    }

    @Test
    public void sequeceMatchHasReturnAndNoDispatch(){
        int oldSize = userDao.getAll().size();
        new BatchCallTask()
                .collectKey(CountryDTO.People.class,CountryDTO.People::getUser)
                .call(userDao,UserDao::insertBatch2)
                .noDispatch()
                .run(countryDTO);
        int newSize = userDao.getAll().size();
        assertEquals(3,newSize-oldSize);
    }

    //这个用例同时测试了类型协变
    //insertBatch2的返回值是List<Integer>类型
    //但是addFinish的参数是int类型，而不是Integer类型，依然能通过
    @Test
    public void sequeceMatchHasReturnAndDispatch(){
        int oldSize = userDao.getAll().size();
        new BatchCallTask()
                .collectKey(CountryDTO.People.class,CountryDTO.People::getUser)
                .call(userDao,UserDao::insertBatch2)
                .dispatch(CountryDTO.People::addFinish)
                .run(countryDTO);
        int newSize = userDao.getAll().size();
        assertEquals(3,newSize-oldSize);

        List<Integer> insertIds = countryDTO.getPeopleList().stream().map(CountryDTO.People::getInsertId).collect(Collectors.toList());
        assertIterableEquals(insertIds, Arrays.asList(30001,30002,30003));
    }

    @Test
    public void sequeceMatchHasReturnAndCountFail(){
        assertThrows(CallResultSizeNotEqualException.class,()->{
            new BatchCallTask()
                    .collectKey(CountryDTO.People.class,CountryDTO.People::getUser)
                    .call(userDao,UserDao::insertBatch3)
                    .dispatch(CountryDTO.People::addFinish)
                    .run(countryDTO);
        });
    }
}
