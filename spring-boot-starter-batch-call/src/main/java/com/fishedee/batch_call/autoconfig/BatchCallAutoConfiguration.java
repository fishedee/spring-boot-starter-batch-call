package com.fishedee.batch_call.autoconfig;

import com.fishedee.batch_call.*;
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

    @Bean("batchCallTask")
    @ConditionalOnMissingBean(BatchCallTask.class)
    @ConditionalOnProperty(value = "spring.batch-call.enable", havingValue = "true")
    public BatchCallTask batchCallTask(){
        return new BatchCallTask();
    }

    @Bean("batchCallTaskFinder")
    @ConditionalOnMissingBean(TaskFinder.class)
    @ConditionalOnProperty(value = "spring.batch-call.enable", havingValue = "true")
    public TaskFinder taskFinder(){
        return new TaskFinder();
    }

    @Bean("batchCallTaskDispatcher")
    @ConditionalOnMissingBean(TaskDispatcher.class)
    @ConditionalOnProperty(value = "spring.batch-call.enable", havingValue = "true")
    public TaskDispatcher taskDispatcher(){
        return new TaskDispatcher();
    }

    @Bean("batchCallTaskExecutor")
    @ConditionalOnMissingBean(TaskExecutor.class)
    @ConditionalOnProperty(value = "spring.batch-call.enable", havingValue = "true")
    public TaskExecutor taskExecutor(){
        return new TaskExecutor();
    }

    @Bean("batchCallTaskChecker")
    @ConditionalOnMissingBean(TaskChecker.class)
    @ConditionalOnProperty(value = "spring.batch-call.enable", havingValue = "true")
    public TaskChecker taskChecker(){
        return new TaskChecker();
    }
}
