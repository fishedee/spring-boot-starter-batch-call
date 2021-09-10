package com.fishedee.batch_call.sample.generic;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SheepDao {
    public List<List<String>> getBatch(List<Integer> ids){
        return ids.stream().map((single)->{
            List<String> result = new ArrayList<>();
            result.add("mm_"+single);
            return result;
        }).collect(Collectors.toList());
    }
}
