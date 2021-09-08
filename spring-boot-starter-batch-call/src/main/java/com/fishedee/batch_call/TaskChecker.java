package com.fishedee.batch_call;

import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
public class TaskChecker {
    public void check(List<Task> taskList){
        String lastType = "";
        for( Task task :taskList){
            Task.Config config = task.getConfig();
            String typeName = config.getClazz().getName()+"_"+config.getGetKeyMethod().getName();
            if( lastType.equals("")){
                lastType =typeName;
            }else if( lastType.equals(typeName) == false ){
                throw new NotSupportMultiplyTypeException();
            }
        }
    }
}
