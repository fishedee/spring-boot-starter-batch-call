package com.fishedee.batch_call.sample;

import com.fishedee.batch_call.BatchCallTask;
import com.fishedee.batch_call.ResultMatchByKey;
import com.fishedee.batch_call.autoconfig.BatchCallAutoConfiguration;
import com.fishedee.batch_call.sample.basic.UserDao;
import com.fishedee.batch_call.sample.recursive.Category;
import com.fishedee.batch_call.sample.recursive.CategoryDTO;
import com.fishedee.batch_call.sample.recursive.CategoryDao;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import java.util.List;

@DataJpaTest(includeFilters = @ComponentScan.Filter(
        type= FilterType.ASSIGNABLE_TYPE,
        classes = {CategoryDao.class}
))
@Import(BatchCallAutoConfiguration.class)
@Slf4j
public class RecursiveTest {

    @Autowired
    private CategoryDao categoryDao;

    //FIXME返回数据以及调用次数assert
    @Test
    public void recursiveRun(){
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(0);

        new BatchCallTask()
                .collectKey(CategoryDTO.class,CategoryDTO::getId)
                .call(categoryDao,CategoryDao::getByParent,new ResultMatchByKey<>(Category::getParentId))
                .groupAndThenDispatch(CategoryDTO::setChildren)
                .setCacheEnabled(true)
                .run(categoryDTO);

        log.info("{}",categoryDTO);
    }

    @Test
    public void recursiveSkipCollectAndRun(){
        List<CategoryDTO> result = new BatchCallTask()
                .collectKey(CategoryDTO.class,CategoryDTO::getId)
                .call(categoryDao,CategoryDao::getByParent,new ResultMatchByKey<>(Category::getParentId))
                .groupAndThenDispatch(CategoryDTO::setChildren)
                .setCacheEnabled(true)
                .firstSkipCollectAndThenCall(0)
                .run();

        log.info("{}",result);
    }
}
