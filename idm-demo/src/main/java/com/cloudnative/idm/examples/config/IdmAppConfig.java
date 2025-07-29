package com.cloudnative.idm.examples.config;

import com.cloudnative.apps.http.IdempotentParamExecuteHandler;
import com.cloudnative.apps.http.IdempotentSpELExecuteHandler;
import com.cloudnative.apps.http.IdempotentTokenExecuteHandler;
import com.cloudnative.idm.aspect.IdempotentAspect;
import com.cloudnative.idm.context.ApplicationContextHolder;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdmAppConfig {
    @Bean
    public IdempotentParamExecuteHandler idempotentParamExecuteHandler(RedissonClient redisClient) {
        return new IdempotentParamExecuteHandler(redisClient);
    }

    @Bean
    public IdempotentSpELExecuteHandler idempotentSpELExecuteHandler(RedissonClient redissonClient) {
        return new IdempotentSpELExecuteHandler(redissonClient);
    }

    @Bean
    public IdempotentTokenExecuteHandler idempotentTokenExecuteHandler(RedissonClient redissonClient) {
        return new IdempotentTokenExecuteHandler(redissonClient);
    }

    @Bean
    public IdempotentAspect idempotentAspect() {
        return new IdempotentAspect();
    }

    @Bean
    public ApplicationContextHolder applicationContextHolder() {
        return new ApplicationContextHolder();
    }
}
