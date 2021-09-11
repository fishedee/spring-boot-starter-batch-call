package com.fishedee.batch_call.sample.recursive;

import lombok.Data;

import javax.persistence.Transient;
import java.util.List;

@Data
public class Category2{
    private int id;

    private int parentId;

    private String name;

    private String path;

    @Transient
    private List<Category2> children;

    public List<Category2> setChildren(List<Category2> children){
        this.children = children;
        return this.children;
    }
}
