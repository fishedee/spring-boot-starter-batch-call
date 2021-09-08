package com.fishedee.batch_call.sample;

import com.fishedee.batch_call.BatchCallTask;
import com.fishedee.batch_call.JsonAssertUtil;
import com.fishedee.batch_call.autoconfig.BatchCallAutoConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest(includeFilters = @ComponentScan.Filter(
        type= FilterType.ASSIGNABLE_TYPE,
        classes = {UserDao.class}
))
@Import(BatchCallAutoConfiguration.class)
public class CountryTest {
    @Autowired
    private BatchCallTask batchCallTask;

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
    public void basicTest(){
        int oldSize = userDao.getAll().size();
        batchCallTask.run("addUser",countryDTO);
        int newSize = userDao.getAll().size();
        assertEquals(3,newSize-oldSize);
    }
}
