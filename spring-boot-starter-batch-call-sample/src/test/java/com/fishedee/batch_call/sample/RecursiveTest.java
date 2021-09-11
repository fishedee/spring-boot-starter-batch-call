package com.fishedee.batch_call.sample;

import com.fishedee.batch_call.*;
import com.fishedee.batch_call.autoconfig.BatchCallAutoConfiguration;
import com.fishedee.batch_call.sample.recursive.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void getAll(){
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(0);

        categoryDao.clearGetByParentCallArgv();

        new BatchCallTask()
                .collectKey(CategoryDTO.class,CategoryDTO::getId)
                .call(categoryDao,CategoryDao::getByParent,new ResultMatchByKey<>(Category::getParentId))
                .groupThenDispatch(CategoryDTO::setChildren)
                .run(categoryDTO);

        JsonAssertUtil.checkEqualStrict("[" +
                "[0]," +
                "[30001,30002]," +
                "[30003,30004,30006]," +
                "[30005,30007]," +
                "[30008]]",categoryDao.getGetByParentCallArgv());

        JsonAssertUtil.checkEqualStrict("{\"id\":0,\"parentId\":0,\"name\":null,\"children\":[" +
                "{\"id\":30001,\"parentId\":0,\"name\":\"分类1\",\"children\":[" +
                    "{\"id\":30003,\"parentId\":30001,\"name\":\"分类3\",\"children\":[" +
                        "{\"id\":30005,\"parentId\":30003,\"name\":\"分类5\",\"children\":[]}" +
                    "]}," +
                    "{\"id\":30004,\"parentId\":30001,\"name\":\"分类4\",\"children\":[]}" +
                "]}," +
                "{\"id\":30002,\"parentId\":0,\"name\":\"分类2\",\"children\":[" +
                    "{\"id\":30006,\"parentId\":30002,\"name\":\"分类6\",\"children\":[" +
                        "{\"id\":30007,\"parentId\":30006,\"name\":\"分类7\",\"children\":[" +
                            "{\"id\":30008,\"parentId\":30007,\"name\":\"分类8\",\"children\":[]}" +
                        "]}" +
                    "]}" +
                "]}" +
                "]}\n",categoryDTO);
    }

    @Test
    public void getSpecifyCategoryAndSubCategory(){
        List<Category> categories = categoryDao.getBatch(Arrays.asList(30002,30003));

        List<CategoryDTO> initCategoryDTO = categories.stream().map(CategoryDTO::new).collect(Collectors.toList());

        categoryDao.clearGetByParentCallArgv();

        new BatchCallTask()
                .collectKey(CategoryDTO.class,CategoryDTO::getId)
                .call(categoryDao,CategoryDao::getByParent,new ResultMatchByKey<>(Category::getParentId))
                .groupThenDispatch(CategoryDTO::setChildren)
                .run(initCategoryDTO);

        JsonAssertUtil.checkEqualStrict("[" +
                "[30002,30003]," +
                "[30006,30005]," +
                "[30007]," +
                "[30008]]",categoryDao.getGetByParentCallArgv());

        JsonAssertUtil.checkEqualStrict("["+
                "{\"id\":30002,\"parentId\":0,\"name\":\"分类2\",\"children\":[" +
                    "{\"id\":30006,\"parentId\":30002,\"name\":\"分类6\",\"children\":[" +
                        "{\"id\":30007,\"parentId\":30006,\"name\":\"分类7\",\"children\":[" +
                            "{\"id\":30008,\"parentId\":30007,\"name\":\"分类8\",\"children\":[]}" +
                        "]}" +
                    "]}" +
                "]}," +
                "{\"id\":30003,\"parentId\":30001,\"name\":\"分类3\",\"children\":[" +
                    "{\"id\":30005,\"parentId\":30003,\"name\":\"分类5\",\"children\":[]}" +
                "]}" +
                "]",initCategoryDTO);
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

        JsonAssertUtil.checkEqualStrict("{\"id\":0,\"parentId\":0,\"name\":null,\"path\":null,\"children\":[" +
                "{\"id\":40001,\"parentId\":0,\"name\":\"分类1\",\"path\":\"0_40001\",\"children\":[" +
                    "{\"id\":40003,\"parentId\":40001,\"name\":\"分类3\",\"path\":\"0_40001_40003\",\"children\":[" +
                        "{\"id\":40005,\"parentId\":40003,\"name\":\"分类5\",\"path\":\"0_40001_40003_40005\",\"children\":[]}" +
                "]},{\"id\":40004,\"parentId\":40001,\"name\":\"分类4\",\"path\":\"0_40001_40004\",\"children\":[]}]}," +
                "{\"id\":40002,\"parentId\":0,\"name\":\"分类2\",\"path\":\"0_40002\",\"children\":[" +
                    "{\"id\":40006,\"parentId\":40002,\"name\":\"分类6\",\"path\":\"0_40002_40006\",\"children\":[" +
                        "{\"id\":40007,\"parentId\":40006,\"name\":\"分类7\",\"path\":\"0_40002_40006_40007\",\"children\":[" +
                            "{\"id\":40008,\"parentId\":40007,\"name\":\"分类8\",\"path\":\"0_40002_40006_40007_40008\",\"children\":[]}" +
                        "]}" +
                    "]}" +
                "]}" +
                "]}\n",category);
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

        JsonAssertUtil.checkEqualStrict("[" +
                "{\"id\":40002,\"parentId\":0,\"name\":\"分类2\",\"path\":\"0_40002\",\"children\":[" +
                    "{\"id\":40006,\"parentId\":40002,\"name\":\"分类6\",\"path\":\"0_40002_40006\",\"children\":[" +
                        "{\"id\":40007,\"parentId\":40006,\"name\":\"分类7\",\"path\":\"0_40002_40006_40007\",\"children\":[" +
                            "{\"id\":40008,\"parentId\":40007,\"name\":\"分类8\",\"path\":\"0_40002_40006_40007_40008\",\"children\":[]}" +
                        "]}" +
                    "]}" +
                "]}," +
                "{\"id\":40003,\"parentId\":40001,\"name\":\"分类3\",\"path\":\"0_40001_40003\",\"children\":[" +
                    "{\"id\":40005,\"parentId\":40003,\"name\":\"分类5\",\"path\":\"0_40001_40003_40005\",\"children\":[]}" +
                "]}" +
                "]\n",target);
    }

    @Test
    public void getSpecifyCategoryAndSubCategory_SingleExist(){
        List<String> paths = Arrays.asList("0_40002");

        List<Category2> allCategory = category2Dao.getBatchByPrefix(paths);

        List<Category2> target = allCategory.stream().filter((single)->{
            return paths.contains(single.getPath());
        }).collect(Collectors.toList());

        new BatchCallTask()
                .collectKey(Category2.class,Category2::getId)
                .find(allCategory,new ResultMatchByKey<>(Category2::getParentId),null)
                .dispatch(Category2::setChildren2)
                .run(target);

        JsonAssertUtil.checkEqualStrict(
                "["+
                "{\"id\":40002,\"parentId\":0,\"name\":\"分类2\",\"path\":\"0_40002\",\"children\":[" +
                    "{\"id\":40006,\"parentId\":40002,\"name\":\"分类6\",\"path\":\"0_40002_40006\",\"children\":[" +
                        "{\"id\":40007,\"parentId\":40006,\"name\":\"分类7\",\"path\":\"0_40002_40006_40007\",\"children\":[" +
                            "{\"id\":40008,\"parentId\":40007,\"name\":\"分类8\",\"path\":\"0_40002_40006_40007_40008\",\"children\":null}" +
                        "]}" +
                    "]}" +
                "]}" +
                        "]",target);
    }

    @Test
    public void getSpecifyCategoryAndSubCategory_NotFound(){
        List<String> paths = Arrays.asList("0_40002_40006_40007_40008");

        List<Category2> allCategory = category2Dao.getBatchByPrefix(paths);

        List<Category2> target = allCategory.stream().filter((single)->{
            return paths.contains(single.getPath());
        }).collect(Collectors.toList());

        assertThrows(CallResultNotFoundException.class,()->{
            new BatchCallTask()
                    .collectKey(Category2.class,Category2::getId)
                    .find(allCategory,new ResultMatchByKey<>(Category2::getParentId))
                    .dispatch(Category2::setChildren2)
                    .run(target);
        });
    }

    @Test
    public void getSpecifyCategoryAndSubCategory_Confuse(){
        List<String> paths = Arrays.asList("0_40001");

        List<Category2> allCategory = category2Dao.getBatchByPrefix(paths);

        List<Category2> target = allCategory.stream().filter((single)->{
            return paths.contains(single.getPath());
        }).collect(Collectors.toList());

        assertThrows(CallResultMultiplyConfuseException.class,()->{
            new BatchCallTask()
                    .collectKey(Category2.class,Category2::getId)
                    .find(allCategory,new ResultMatchByKey<>(Category2::getParentId),null)
                    .dispatch(Category2::setChildren2)
                    .run(target);
        });
    }

    @Test
    public void getSpecifyCategoryAndSubCategory_CloseCached(){
        List<String> paths = Arrays.asList("0_40001");

        List<Category2> allCategory = category2Dao.getBatchByPrefix(paths);

        List<Category2> target = allCategory.stream().filter((single)->{
            return paths.contains(single.getPath());
        }).collect(Collectors.toList());

        assertThrows(InvalidArgumentException.class,()->{
            new BatchCallTask()
                    .collectKey(Category2.class,Category2::getId)
                    .find(allCategory,new ResultMatchByKey<>(Category2::getParentId),null)
                    .dispatch(Category2::setChildren2)
                    .setCacheEnabled(false)
                    .run(target);
        });
    }
}
