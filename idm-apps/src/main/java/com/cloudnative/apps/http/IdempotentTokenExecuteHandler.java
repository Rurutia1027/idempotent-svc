package com.cloudnative.apps.http;

import com.cloudnative.idm.aspect.handler.AbstractIdempotentExecuteHandler;
import com.cloudnative.idm.aspect.wrapper.IdempotentParamWrapper;
import com.cloudnative.idm.service.IdempotentTokenService;
import org.aspectj.lang.ProceedingJoinPoint;

public class IdempotentTokenExecuteHandler extends AbstractIdempotentExecuteHandler
        implements IdempotentTokenService {

    @Override
    protected IdempotentParamWrapper buildWrapper(ProceedingJoinPoint joinPoint) {
        return null;
    }

    @Override
    public void handler(IdempotentParamWrapper wrapper) {

    }

    @Override
    public void exceptionProcessing() {
        super.exceptionProcessing();
    }

    @Override
    public void postProcessing() {
        super.postProcessing();
    }
}
