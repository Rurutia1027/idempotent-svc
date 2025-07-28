package com.cloudnative.apps.http;

import com.cloudnative.idm.aspect.handler.AbstractIdempotentExecuteHandler;
import com.cloudnative.idm.aspect.wrapper.IdempotentParamWrapper;
import com.cloudnative.idm.service.IdempotentSpELHTTPService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.redisson.api.RedissonClient;

@RequiredArgsConstructor
public class IdempotentSpELExecuteHandler extends AbstractIdempotentExecuteHandler
        implements IdempotentSpELHTTPService {
    private final RedissonClient redissonClient;

    @Override
    protected IdempotentParamWrapper buildWrapper(ProceedingJoinPoint joinPoint) {
        return null;
    }

    @Override
    public void handler(IdempotentParamWrapper wrapper) {

    }

    @Override
    public void exceptionProcessing() {

    }

    @Override
    public void postProcessing() {

    }
}