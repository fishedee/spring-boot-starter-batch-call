package com.fishedee.batch_call.autoconfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix="spring.batch-call")
public class BatchCallProperties {
    private boolean enable;

    private int batchSize = 1000;
}
