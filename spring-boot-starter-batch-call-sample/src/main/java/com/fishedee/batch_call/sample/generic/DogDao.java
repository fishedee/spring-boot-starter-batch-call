package com.fishedee.batch_call.sample.generic;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DogDao {
    public List<List<Dog>> getBatch(List<Integer> ids){
        return ids.stream().map((single)->{
            List<Dog> result = new ArrayList<>();
            Dog dog = new Dog();
            dog.setName("dog_"+single);
            result.add(dog);
            return result;
        }).collect(Collectors.toList());
    }
}
