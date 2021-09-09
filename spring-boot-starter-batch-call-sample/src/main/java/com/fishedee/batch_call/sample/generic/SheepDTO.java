package com.fishedee.batch_call.sample.generic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SheepDTO extends DataGenericDTO2<String>{

    private Integer id;
}
