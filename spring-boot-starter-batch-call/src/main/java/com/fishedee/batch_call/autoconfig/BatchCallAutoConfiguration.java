package com.fishedee.batch_call.autoconfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractApplicationContext;

@Slf4j
@Configuration
@EnableConfigurationProperties(BatchCallProperties.class)
public class BatchCallAutoConfiguration {
    private final AbstractApplicationContext applicationContext;

    private final BatchCallProperties properties;

    public BatchCallAutoConfiguration(AbstractApplicationContext applicationContext, BatchCallProperties properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;
    }
}
