package com.cloudnative.idem.core.handler;

import com.cloudnative.idem.api.annotation.Idempotent;
import com.cloudnative.idem.core.param.IdempotentParamWrapper;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Abstract class for idempotent execute handler
 */
public abstract class AbstractIdempotentExecuteHandler implements IdempotentExecuteHandler {
    /**
     * Construct parameter wrapper instance required during idempotent verification.
     *
     * @param joinPoint AOP method processor
     * @return idempotent parameter wrapper
     */
    protected abstract IdempotentParamWrapper buildWrapper(ProceedingJoinPoint joinPoint);

    /**
     * Idempotent execute entry point.
     *
     * @param joinPoint AOP join point instance.
     * @param idempotent idempotent annotation
     */
    public void execute(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        IdempotentParamWrapper idempotentParamWrapper =
                buildWrapper(joinPoint).setIdempotent(idempotent);
        handler(idempotentParamWrapper);
    }

}
