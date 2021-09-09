package com.fishedee.batch_call.sample;

import com.fishedee.batch_call.autoconfig.BatchCallAutoConfiguration;
import com.fishedee.batch_call.sample.basic.RecipeDTO;
import com.fishedee.batch_call.sample.generic.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

@DataJpaTest
@Import(BatchCallAutoConfiguration.class)
@Slf4j
public class FinderSuccessTest {


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
    /*
    @Test
    public void testBasic(){
        RecipeDTO.Step step = new RecipeDTO.Step();
        step.setUserId(10001);

        List<Task> taskList = finder.find("getUser",step);
        assertEquals(taskList.size(),1);
        assertEquals(step,taskList.get(0).getInstance());
        assertEquals(step.getUserId(),taskList.get(0).getKey());
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


    @Test
    public void testSet(){
        //初始化数据
        Set<RecipeDTO.Step> hashSet = new LinkedHashSet<>();
        for( RecipeDTO.Step step  :recipeDTO.getStepList()){
            hashSet.add(step);
        }

        List<Task> taskList = finder.find("getUser",hashSet);

        assertEquals(taskList.size(),3);
        for( int i = 0 ;i !=3;i++){
            assertEquals(recipeDTO.getStepList().get(i),taskList.get(i).getInstance());
        }
        for( int i = 0 ;i !=3;i++){
            assertEquals(recipeDTO.getStepList().get(i).getUserId(),taskList.get(i).getKey());
        }
    }

    @Test
    public void testMap(){
        //初始化数据
        Map<Integer,RecipeDTO.Step> hashMap = new LinkedHashMap<>();
        int id = 1;
        for( RecipeDTO.Step step  :recipeDTO.getStepList()){
            hashMap.put(id,step);
            id++;
        }

        List<Task> taskList = finder.find("getUser",hashMap);

        assertEquals(taskList.size(),4);
        for( int i = 0 ;i !=4;i++){
            assertEquals(recipeDTO.getStepList().get(i),taskList.get(i).getInstance());
        }
        for( int i = 0 ;i !=4;i++){
            assertEquals(recipeDTO.getStepList().get(i).getUserId(),taskList.get(i).getKey());
        }
    }

    @Test
    public void combine(){
        Map<Integer,RecipeDTO> hashMap = new LinkedHashMap<>();
        for( int i = 0 ;i != 10;i++){
            hashMap.put(i,recipeDTO);
        }

        List<Task> taskList = finder.find("getUser",hashMap);

        assertEquals(taskList.size(),40);
        for( int i = 0 ;i !=40;i++){
            assertEquals(recipeDTO.getStepList().get(i%4),taskList.get(i).getInstance());
        }
        for( int i = 0 ;i !=40;i++){
            assertEquals(recipeDTO.getStepList().get(i%4).getUserId(),taskList.get(i).getKey());
        }
    }

    //泛型
    @Test
    public void invokeMethodGeneric(){
        List<CatDTO> catList = new ArrayList<>();
        catList.add(new CatDTO(1001,""));
        catList.add(new CatDTO(1002,""));
        catList.add(new CatDTO(1003,""));
        catList.add(new CatDTO(1004,""));

        List<Task> taskList = finder.find("getCat",catList);

        assertEquals(taskList.size(),4);
        for( int i = 0 ;i !=4;i++){
            assertEquals(catList.get(i),taskList.get(i).getInstance());
        }
        for( int i = 0 ;i !=4;i++){
            assertEquals(catList.get(i).getId(),taskList.get(i).getKey());
        }
    }


    //FIXME 不支持callbcakMethod的泛型参数是纯碎的泛型参数T
    //只支持callBackMethod的泛型参数是容器包装类型
    //因为泛型参数是纯粹的泛型参数时，方法的参数难以填写，getMethod(XXXX)
    @Test
    public void callbackMethodGenericNormalArgument(){
        List<SheepDTO> sheepList = new ArrayList<>();
        sheepList.add(new SheepDTO(1001));
        sheepList.add(new SheepDTO(1002));
        sheepList.add(new SheepDTO(1003));
        sheepList.add(new SheepDTO(1004));

        List<Task> taskList = finder.find("getSheep",sheepList);

        assertEquals(taskList.size(),4);
        for( int i = 0 ;i !=4;i++){
            assertEquals(sheepList.get(i),taskList.get(i).getInstance());
        }
        for( int i = 0 ;i !=4;i++){
            assertEquals(sheepList.get(i).getId(),taskList.get(i).getKey());
        }
    }

    @Test
    public void callbackMethodGenericListArgument(){
        List<DogDTO> dogList = new ArrayList<>();
        dogList.add(new DogDTO(1001));
        dogList.add(new DogDTO(1002));
        dogList.add(new DogDTO(1003));
        dogList.add(new DogDTO(1004));

        List<Task> taskList = finder.find("getDog",dogList);

        assertEquals(taskList.size(),4);
        for( int i = 0 ;i !=4;i++){
            assertEquals(dogList.get(i),taskList.get(i).getInstance());
        }
        for( int i = 0 ;i !=4;i++){
            assertEquals(dogList.get(i).getId(),taskList.get(i).getKey());
        }
    }
    */
}
