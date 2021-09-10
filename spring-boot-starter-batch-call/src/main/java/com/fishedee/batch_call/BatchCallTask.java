package com.fishedee.batch_call;

import com.fishedee.batch_call.autoconfig.BatchCallProperties;

import java.util.function.Function;

public class BatchCallTask {
    public <KeyObjectType,KeyType> BatchCall<KeyObjectType,KeyType> collectKey(Class<KeyObjectType> clazz,Function<KeyObjectType,KeyType> collectFunc){
        BatchCallProperties properties = TaskRunner.sinlegton().getProperties();
        Config config = new Config();
        config.setBatchSize(properties.getBatchSize());
        config.setCacheEnabled(properties.isCacheEnabled());
        config.setCollectFunc(collectFunc);
        config.setKeyObjectType(clazz);
        return new BatchCall<KeyObjectType,KeyType>(config);
    }
}
