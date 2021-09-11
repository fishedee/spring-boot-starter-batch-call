package com.fishedee.batch_call.sample.api;

import com.fishedee.batch_call.BatchCallTask;
import com.fishedee.batch_call.ResultMatchByKey;
import com.fishedee.batch_call.sample.recursive.Category;
import com.fishedee.batch_call.sample.recursive.CategoryDTO;
import com.fishedee.batch_call.sample.recursive.CategoryDao;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryDao categoryDao;

    @Data
    public static class Req{
        private List<Integer> categoryIds;
    }

    @PostMapping("/getBatch")
    public List<CategoryDTO> getBatch(@RequestBody  Req req){

        List<Category> categories = categoryDao.getBatch(req.categoryIds);

        List<CategoryDTO> initCategoryDTO = categories.stream().map(CategoryDTO::new).collect(Collectors.toList());

        new BatchCallTask()
                .collectKey(CategoryDTO.class,CategoryDTO::getId)
                .call(categoryDao, CategoryDao::getByParent,new ResultMatchByKey<>(Category::getParentId))
                .groupThenDispatch(CategoryDTO::setChildren)
                .run(initCategoryDTO);
        return initCategoryDTO;
    }
}
