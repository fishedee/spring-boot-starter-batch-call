package com.fishedee.batch_call.sample.basic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeDTO2 {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class KK{
        private RecipeDTO recipeDTO;
    }

    private List<KK> kkList = new ArrayList<>();
}
