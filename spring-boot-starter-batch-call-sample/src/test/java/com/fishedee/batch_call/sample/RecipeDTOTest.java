package com.fishedee.batch_call.sample;

import com.fishedee.batch_call.BatchCallTask;
import com.fishedee.batch_call.JsonAssertUtil;
import com.fishedee.batch_call.autoconfig.BatchCallAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(BatchCallAutoConfiguration.class)
public class RecipeDTOTest{
    @Autowired
    private BatchCallTask batchCallTask;

    @Test
    public void basicTest(){
        //初始化数据
        RecipeDTO.Step step = new RecipeDTO.Step();
        step.setUserId(10001L);
        RecipeDTO.Step step2 = new RecipeDTO.Step();
        step2.setUserId(10003L);
        RecipeDTO.Step step3 = new RecipeDTO.Step();
        step3.setUserId(10002L);
        RecipeDTO.Step step4 = new RecipeDTO.Step();
        step4.setUserId(10001L);


        RecipeDTO recipeDTO = new RecipeDTO();
        recipeDTO.getStepList().add(step);
        recipeDTO.getStepList().add(step2);
        recipeDTO.getStepList().add(step3);
        recipeDTO.getStepList().add(step4);

        batchCallTask.run("getUserName",recipeDTO);

        JsonAssertUtil.checkEqualNotStrict("{}",recipeDTO);
    }
}