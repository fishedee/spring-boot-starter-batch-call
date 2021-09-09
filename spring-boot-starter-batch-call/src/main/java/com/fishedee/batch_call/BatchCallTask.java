package com.fishedee.batch_call;

import java.util.function.Function;

public class BatchCallTask {
    public <KeyObjectType,KeyType> BatchCall<KeyObjectType,KeyType> collectKey(Class<KeyObjectType> clazz,Function<KeyObjectType,KeyType> collectFunc){
        Config config = new Config();
        config.setCollectFunc(collectFunc);
        config.setKeyObjectType(clazz);
        return new BatchCall<KeyObjectType,KeyType>(config);
    }
}
