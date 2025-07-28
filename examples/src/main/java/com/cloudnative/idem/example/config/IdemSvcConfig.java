package com.cloudnative.idem.example.config;

import com.cloudnative.idem.core.aspect.IdempotentAspect;
import com.cloudnative.idem.core.context.ApplicationContextHolder;
import com.cloudnative.idem.http.IdempotentParamExecuteHandler;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdemSvcConfig {
    @Bean
    public IdempotentParamExecuteHandler idempotentParamExecuteHandler(RedissonClient redisClient) {
        return new IdempotentParamExecuteHandler(redisClient);
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
