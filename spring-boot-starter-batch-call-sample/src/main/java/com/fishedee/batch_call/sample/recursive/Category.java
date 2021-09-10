package com.fishedee.batch_call.sample.recursive;

import lombok.Data;

@Data
public class Category {
    private int id;

    private int parentId;

    private String name;
}
