package com.fishedee.batch_call.sample;

import com.fishedee.batch_call.JsonAssertUtil;
import com.fishedee.batch_call.Task;
import com.fishedee.batch_call.TaskFinder;
import com.fishedee.batch_call.autoconfig.BatchCallAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

@DataJpaTest
@Import(BatchCallAutoConfiguration.class)
@Slf4j
public class FinderTest {

    @Autowired
    private TaskFinder finder;

    private RecipeDTO recipeDTO;

    @BeforeEach
    public void setUp(){
        //初始化数据
        RecipeDTO.Step step = new RecipeDTO.Step();
        step.setUserId(10001);
        RecipeDTO.Step step2 = new RecipeDTO.Step();
        step2.setUserId(10003);
        RecipeDTO.Step step3 = new RecipeDTO.Step();
        step3.setUserId(10002);
        RecipeDTO.Step step4 = new RecipeDTO.Step();
        step4.setUserId(10001);


        recipeDTO = new RecipeDTO();
        recipeDTO.getStepList().add(step);
        recipeDTO.getStepList().add(step2);
        recipeDTO.getStepList().add(step3);
        recipeDTO.getStepList().add(step4);
    }

    @Test
    public void testBasic(){
        RecipeDTO.Step step = new RecipeDTO.Step();
        step.setUserId(10001);
    }

    @Test
    public void testMap(){

    }

    @Test
    public void testSet(){

    }

    @Test
    public void testList(){
        List<Task> taskList = finder.find("getUser",recipeDTO);

        assertEquals(taskList.size(),4);
        for( int i = 0 ;i !=4;i++){
            assertEquals(recipeDTO.getStepList().get(i),taskList.get(i).getInstance());
        }
        for( int i = 0 ;i !=4;i++){
            assertEquals(recipeDTO.getStepList().get(i).getUserId(),taskList.get(i).getKey());
        }
    }

}
