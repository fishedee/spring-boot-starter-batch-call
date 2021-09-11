package com.fishedee.batch_call.sample;

import com.fishedee.batch_call.BatchCallTask;
import com.fishedee.batch_call.JsonAssertUtil;
import com.fishedee.batch_call.ResultMatchByKey;
import com.fishedee.batch_call.autoconfig.BatchCallAutoConfiguration;
import com.fishedee.batch_call.sample.basic.RecipeDTO;
import com.fishedee.batch_call.sample.basic.User;
import com.fishedee.batch_call.sample.basic.UserDao;
import com.fishedee.batch_call.sample.generic.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Collectors;

@DataJpaTest(includeFilters = @ComponentScan.Filter(
        type= FilterType.ASSIGNABLE_TYPE,
        classes = {UserDao.class,CatDao.class,SheepDao.class,DogDao.class}
))
@Import(BatchCallAutoConfiguration.class)
@Slf4j
public class FinderTest {


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

    @Autowired
    private UserDao userDao;

    private void runTask(Object target){
        new BatchCallTask()
                .collectKey(RecipeDTO.Step.class,RecipeDTO.Step::getUserId)
                .call(userDao,UserDao::getBatch,new ResultMatchByKey<>(User::getId))
                .dispatch(RecipeDTO.Step::setUser)
                .run(target);
    }

    @Test
    public void testBasic(){
        RecipeDTO.Step step = new RecipeDTO.Step();
        step.setUserId(10001);

        this.runTask(step);
        assertEquals(10001,step.getUserId());
        assertEquals("fish",step.getName());
        assertEquals(12,step.getLevel());
    }
    @Test
    public void testList(){
        this.runTask(recipeDTO);

        assertEquals(4,recipeDTO.getStepList().size());
        JsonAssertUtil.checkEqualStrict("{name:'fish',level:12,userId:10001,decription:null}",recipeDTO.getStepList().get(0));
        JsonAssertUtil.checkEqualStrict("{name:'dog',level:56,userId:10003,decription:null}",recipeDTO.getStepList().get(1));
        JsonAssertUtil.checkEqualStrict("{name:'cat',level:34,userId:10002,decription:null}",recipeDTO.getStepList().get(2));
        JsonAssertUtil.checkEqualStrict("{name:'fish',level:12,userId:10001,decription:null}",recipeDTO.getStepList().get(3));
    }

    @Test
    public void testSet(){
        //初始化数据
        Set<RecipeDTO.Step> hashSet = new LinkedHashSet<>();
        for( RecipeDTO.Step step  :recipeDTO.getStepList()){
            hashSet.add(step);
        }

        this.runTask(hashSet);

        assertEquals(3,hashSet.size());
        int i = 0 ;
        for( RecipeDTO.Step step :hashSet ){
            if( i == 0 ){
                JsonAssertUtil.checkEqualStrict("{name:'fish',level:12,userId:10001,decription:null}",step);
            }else if ( i == 1 ){
                JsonAssertUtil.checkEqualStrict("{name:'dog',level:56,userId:10003,decription:null}",step);
            }else{
                JsonAssertUtil.checkEqualStrict("{name:'cat',level:34,userId:10002,decription:null}",step);
            }
            i++;
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

        this.runTask(hashMap);

        assertEquals(4,hashMap.size());
        int i = 0 ;
        for( RecipeDTO.Step step :hashMap.values() ){
            if( i == 0 || i == 3 ){
                JsonAssertUtil.checkEqualStrict("{name:'fish',level:12,userId:10001,decription:null}",step);
            }else if ( i == 1 ){
                JsonAssertUtil.checkEqualStrict("{name:'dog',level:56,userId:10003,decription:null}",step);
            }else{
                JsonAssertUtil.checkEqualStrict("{name:'cat',level:34,userId:10002,decription:null}",step);
            }
            i++;
        }
    }

    @Test
    public void combineNoBatch() {
        Map<Integer, RecipeDTO> hashMap = new LinkedHashMap<>();
        for (int i = 0; i != 10; i++) {
            hashMap.put(i, recipeDTO);
        }

        //收集userId
        List<Integer> userIds = hashMap.values().stream().map((recipeDTO)->{
            return recipeDTO.getStepList().stream().map((single)->single.getUserId()).collect(Collectors.toList());
        }).reduce((a,b)->{
            a.addAll(b);
            return a;
        }).get();

        //批量调用
        Map<Integer,User> userMap = userDao.getBatch(userIds).stream()
                .collect(Collectors.toMap(e->e.getId(), e->e));

        hashMap.values().stream().forEach((recipeDTO -> {
            recipeDTO.getStepList().forEach((step)->{
                step.setUser(userMap.get(step.getUserId()));
            });
        }));

        //校验
        for( RecipeDTO recipeDTO :hashMap.values()){
            assertEquals(4,recipeDTO.getStepList().size());
            JsonAssertUtil.checkEqualStrict("{name:'fish',level:12,userId:10001,decription:null}",recipeDTO.getStepList().get(0));
            JsonAssertUtil.checkEqualStrict("{name:'dog',level:56,userId:10003,decription:null}",recipeDTO.getStepList().get(1));
            JsonAssertUtil.checkEqualStrict("{name:'cat',level:34,userId:10002,decription:null}",recipeDTO.getStepList().get(2));
            JsonAssertUtil.checkEqualStrict("{name:'fish',level:12,userId:10001,decription:null}",recipeDTO.getStepList().get(3));
        }
    }

    @Test
    public void combine(){
        Map<Integer,RecipeDTO> hashMap = new LinkedHashMap<>();
        for( int i = 0 ;i != 10;i++){
            hashMap.put(i,recipeDTO);
        }

        this.runTask(hashMap);

        for( RecipeDTO recipeDTO :hashMap.values()){
            assertEquals(4,recipeDTO.getStepList().size());
            JsonAssertUtil.checkEqualStrict("{name:'fish',level:12,userId:10001,decription:null}",recipeDTO.getStepList().get(0));
            JsonAssertUtil.checkEqualStrict("{name:'dog',level:56,userId:10003,decription:null}",recipeDTO.getStepList().get(1));
            JsonAssertUtil.checkEqualStrict("{name:'cat',level:34,userId:10002,decription:null}",recipeDTO.getStepList().get(2));
            JsonAssertUtil.checkEqualStrict("{name:'fish',level:12,userId:10001,decription:null}",recipeDTO.getStepList().get(3));
        }
    }

    @Autowired
    private CatDao catDao;

    //泛型
    @Test
    public void invokeMethodGeneric(){
        List<CatDTO> catList = new ArrayList<>();
        catList.add(new CatDTO(1001,""));
        catList.add(new CatDTO(1002,""));
        catList.add(new CatDTO(1003,""));
        catList.add(new CatDTO(1004,""));

        new BatchCallTask()
                .collectKey(CatDTO.class,CatDTO::getId)
                .call(catDao,CatDao::getBatch,new ResultMatchByKey<>(Cat::getId))
                .dispatch(CatDTO::setCat)
                .run(catList);

        assertEquals(4,catList.size());
        JsonAssertUtil.checkEqualStrict("{id:1001,family:'setFamily_1'}",catList.get(0));
        JsonAssertUtil.checkEqualStrict("{id:1002,family:'setFamily_2'}",catList.get(1));
        JsonAssertUtil.checkEqualStrict("{id:1003,family:'setFamily_3'}",catList.get(2));
        JsonAssertUtil.checkEqualStrict("{id:1004,family:'setFamily_4'}",catList.get(3));
    }

    @Autowired
    private SheepDao sheepDao;

    @Test
    public void callbackMethodGenericNormalArgument(){
        List<SheepDTO> sheepList = new ArrayList<>();
        sheepList.add(new SheepDTO(1001));
        sheepList.add(new SheepDTO(1002));
        sheepList.add(new SheepDTO(1003));
        sheepList.add(new SheepDTO(1004));

        new BatchCallTask()
                .collectKey(SheepDTO.class,SheepDTO::getId)
                .call(sheepDao,SheepDao::getBatch)
                .dispatch(SheepDTO::setName)
                .run(sheepList);

        assertEquals(4,sheepList.size());
        JsonAssertUtil.checkEqualStrict("{id:1001,name:['mm_1001']}",sheepList.get(0));
        JsonAssertUtil.checkEqualStrict("{id:1002,name:['mm_1002']}",sheepList.get(1));
        JsonAssertUtil.checkEqualStrict("{id:1003,name:['mm_1003']}",sheepList.get(2));
        JsonAssertUtil.checkEqualStrict("{id:1004,name:['mm_1004']}",sheepList.get(3));
    }

    @Autowired
    private DogDao dogDao;

    @Test
    public void callbackMethodGenericListArgument(){
        List<DogDTO> dogList = new ArrayList<>();
        dogList.add(new DogDTO(1001));
        dogList.add(new DogDTO(1002));
        dogList.add(new DogDTO(1003));
        dogList.add(new DogDTO(1004));

        new BatchCallTask()
                .collectKey(DogDTO.class,DogDTO::getId)
                .call(dogDao,DogDao::getBatch)
                .dispatch(DogDTO::setName)
                .run(dogList);

    }
}
