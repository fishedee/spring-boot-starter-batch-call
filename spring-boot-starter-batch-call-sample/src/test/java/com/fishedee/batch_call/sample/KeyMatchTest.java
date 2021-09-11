package com.fishedee.batch_call.sample;

import com.fishedee.batch_call.*;
import com.fishedee.batch_call.autoconfig.BatchCallAutoConfiguration;
import com.fishedee.batch_call.sample.basic.*;
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
        classes = {UserDao.class, CarDao.class}
))
@Import(BatchCallAutoConfiguration.class)
public class KeyMatchTest {

    private RecipeDTO recipeDTO;

    private ParkingDTO parkingDTO;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CarDao carDao;

    @BeforeEach
    public void setUp() {
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

        ParkingDTO.Floor floor = new ParkingDTO.Floor();
        floor.setDriverId(10001);
        ParkingDTO.Floor floor2 = new ParkingDTO.Floor();
        floor2.setDriverId(10002);
        ParkingDTO.Floor floor3 = new ParkingDTO.Floor();
        floor3.setDriverId(10003);

        parkingDTO = new ParkingDTO();
        parkingDTO.getFloorList().add(floor);
        parkingDTO.getFloorList().add(floor2);
        parkingDTO.getFloorList().add(floor3);
    }

    @Test
    public void keyMatchAndDispatch(){
        new BatchCallTask()
            .collectKey(RecipeDTO.Step.class,RecipeDTO.Step::getUserId)
            .call(userDao,UserDao::getBatch,new ResultMatchByKey<>(User::getId))
            .dispatch(RecipeDTO.Step::setUser)
            .run(recipeDTO);

        assertEquals(4,recipeDTO.getStepList().size());
        JsonAssertUtil.checkEqualStrict("{name:'fish',level:12,userId:10001,decription:null}",recipeDTO.getStepList().get(0));
        JsonAssertUtil.checkEqualStrict("{name:'dog',level:56,userId:10003,decription:null}",recipeDTO.getStepList().get(1));
        JsonAssertUtil.checkEqualStrict("{name:'cat',level:34,userId:10002,decription:null}",recipeDTO.getStepList().get(2));
        JsonAssertUtil.checkEqualStrict("{name:'fish',level:12,userId:10001,decription:null}",recipeDTO.getStepList().get(3));
    }

    @Test
    public void keyMatchAndDispatchList(){
        new BatchCallTask()
                .collectKey(ParkingDTO.Floor.class,ParkingDTO.Floor::getDriverId)
                .call(carDao,CarDao::getByDriverId,new ResultMatchByKey<>(Car::getDriverId))
                .groupThenDispatch(ParkingDTO.Floor::setCarList)
                .run(parkingDTO);

        assertEquals(3,parkingDTO.getFloorList().size());
        JsonAssertUtil.checkEqualStrict("{driverId:10001,carList:[{id:20001,driverId:10001,name:'车1',color:'red'}]}",parkingDTO.getFloorList().get(0));
        JsonAssertUtil.checkEqualStrict("{driverId:10002,carList:[{id:20002,driverId:10002,name:'车2',color:'green'},{id:20003,driverId:10002,name:'车3',color:'blue'}]}",parkingDTO.getFloorList().get(1));
        JsonAssertUtil.checkEqualStrict("{driverId:10003,carList:[]}",parkingDTO.getFloorList().get(2));
    }

    @Test
    public void keyMatchAndDispatchDefaultNotNull(){
        RecipeDTO.Step step = new RecipeDTO.Step();
        step.setUserId(10005);

        User defaultUser = new User(0,"default",123);
        new BatchCallTask()
                .collectKey(RecipeDTO.Step.class,RecipeDTO.Step::getUserId)
                .call(userDao,UserDao::getBatch,new ResultMatchByKey<>(User::getId),defaultUser)
                .dispatch(RecipeDTO.Step::setUser)
                .run(step);

        JsonAssertUtil.checkEqualStrict("{name:'default',level:123,userId:10005,decription:null}",step);
    }

    @Test
    public void keyMatchAndDispatchDefaultNull(){
        RecipeDTO.Step step = new RecipeDTO.Step();
        step.setUserId(10005);

        new BatchCallTask()
                .collectKey(RecipeDTO.Step.class,RecipeDTO.Step::getUserId)
                .call(userDao,UserDao::getBatch,new ResultMatchByKey<>(User::getId),null)
                .dispatch(RecipeDTO.Step::setUser2)
                .run(step);

        JsonAssertUtil.checkEqualStrict("{name:null,level:0,userId:10005,decription:null}",step);
    }

    @Test
    public void keyMatchAndDispatchConfuse(){
        assertThrows(CallResultMultiplyConfuseException.class,()->{
            new BatchCallTask()
                    .collectKey(ParkingDTO.Floor.class,ParkingDTO.Floor::getDriverId)
                    .call(carDao,CarDao::getByDriverId,new ResultMatchByKey<>(Car::getDriverId))
                    .dispatch(ParkingDTO.Floor::setCarList2)
                    .run(parkingDTO);
        });
    }

    @Test
    public void keyMatchAndDispatchNotFound(){
        assertThrows(CallResultNotFoundException.class,()->{
            RecipeDTO.Step step = new RecipeDTO.Step();
            step.setUserId(10005);

            new BatchCallTask()
                    .collectKey(RecipeDTO.Step.class,RecipeDTO.Step::getUserId)
                    .call(userDao,UserDao::getBatch,new ResultMatchByKey<>(User::getId))
                    .dispatch(RecipeDTO.Step::setUser)
                    .run(step);
        });
    }
}