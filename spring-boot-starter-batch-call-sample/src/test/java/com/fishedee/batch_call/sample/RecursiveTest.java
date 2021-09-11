package com.fishedee.batch_call.sample;

import com.fishedee.batch_call.BatchCallTask;
import com.fishedee.batch_call.ResultMatchByKey;
import com.fishedee.batch_call.autoconfig.BatchCallAutoConfiguration;
import com.fishedee.batch_call.sample.recursive.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@DataJpaTest(includeFilters = @ComponentScan.Filter(
        type= FilterType.ASSIGNABLE_TYPE,
        classes = {CategoryDao.class, Category2Dao.class}
))
@Import(BatchCallAutoConfiguration.class)
@Slf4j
public class RecursiveTest {

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private Category2Dao category2Dao;

    //FIXME返回数据以及调用次数assert
    @Test
    public void getAll(){
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(0);

        new BatchCallTask()
                .collectKey(CategoryDTO.class,CategoryDTO::getId)
                .call(categoryDao,CategoryDao::getByParent,new ResultMatchByKey<>(Category::getParentId))
                .groupThenDispatch(CategoryDTO::setChildren)
                .run(categoryDTO);

        log.info("{}",categoryDTO);
    }

    @Test
    public void getSpecifyCategoryAndSubCategory(){
        List<Category> categories = categoryDao.getBatch(Arrays.asList(30002,30003));

        List<CategoryDTO> initCategoryDTO = categories.stream().map(CategoryDTO::new).collect(Collectors.toList());

        new BatchCallTask()
                .collectKey(CategoryDTO.class,CategoryDTO::getId)
                .call(categoryDao,CategoryDao::getByParent,new ResultMatchByKey<>(Category::getParentId))
                .groupThenDispatch(CategoryDTO::setChildren)
                .run(initCategoryDTO);

        log.info("{}",initCategoryDTO);
    }

    @Test
    public void getAll2(){
        List<Category2> allCategory = category2Dao.getBatchByPrefix(Arrays.asList("0"));

        Category2 category = new Category2();
        category.setId(0);

        new BatchCallTask()
                .collectKey(Category2.class,Category2::getId)
                .find(allCategory,new ResultMatchByKey<>(Category2::getParentId))
                .groupThenDispatch(Category2::setChildren)
                .run(category);

        log.info("{}",category);
    }

    @Test
    public void getSpecifyCategoryAndSubCategory2(){
        List<String> paths = Arrays.asList("0_40002","0_40001_40003");

        List<Category2> allCategory = category2Dao.getBatchByPrefix(paths);

        List<Category2> target = allCategory.stream().filter((single)->{
            return paths.contains(single.getPath());
        }).collect(Collectors.toList());

        new BatchCallTask()
                .collectKey(Category2.class,Category2::getId)
                .find(allCategory,new ResultMatchByKey<>(Category2::getParentId))
                .groupThenDispatch(Category2::setChildren)
                .run(target);

        log.info("{}",target);
    }
}
