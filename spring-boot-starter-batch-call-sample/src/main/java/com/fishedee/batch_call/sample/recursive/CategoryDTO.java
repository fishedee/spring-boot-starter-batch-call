package com.fishedee.batch_call.sample.recursive;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@Slf4j
public class CategoryDTO {

    private int id;

    private int parentId;

    private String name;

    private List<CategoryDTO> children;

    public CategoryDTO(Category category){
        this.id = category.getId();
        this.parentId = category.getParentId();
        this.name = category.getName();
    }

    public List<CategoryDTO> setChildren(List<Category> category){
        this.children = category.stream().map((single)->{
            return new CategoryDTO(single);
        }).collect(Collectors.toList());
        return this.children;
    }

    public void setChildren2(List<CategoryDTO> children){
        this.children = children;
    }
}
