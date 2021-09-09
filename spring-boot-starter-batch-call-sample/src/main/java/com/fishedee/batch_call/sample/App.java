package com.fishedee.batch_call.sample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@Slf4j
public class App 
{
    public static void main( String[] args )
    {
        SpringApplication.run(App.class,args);
    }


    public<T,U> void go(Function<T,U> func )throws Exception{

        log.info("data {}", func.getClass());
        Class clazz = func.getClass();
        Field[] fields = clazz.getFields();
        Arrays.stream(fields).forEach((single)->{
            log.info("field {}",single);
        });

        Method[] methods = clazz.getMethods();
        Arrays.stream(methods).forEach((single)->{
            log.info("method {}",single);
        });
    }

    @PostConstruct
    public void test()throws Exception{
        go((List<String> data)->{
            List<Integer> reuslt = new ArrayList<Integer>();
            reuslt.add(1);
            reuslt.add(3);
            return reuslt;
        });

        go(MyBatch::getBatch);
    }
}
