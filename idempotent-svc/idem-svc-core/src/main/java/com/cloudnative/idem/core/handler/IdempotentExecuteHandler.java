package com.cloudnative.idem.core.handler;

import com.cloudnative.idem.api.annotation.Idempotent;
import com.cloudnative.idem.core.param.IdempotentParamWrapper;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * idempotent execution handler
 */
public interface IdempotentExecuteHandler {
    /**
     * idempotent execution handler
     *
     * @param wrapper parameter wrapper
     */
    void handler(IdempotentParamWrapper wrapper);

    /**
     * idempotent execution logic
     *
     * @param joinPoint  AOP entry point
     * @param idempotent idempotent annotation
     */
    void execute(ProceedingJoinPoint joinPoint, Idempotent idempotent);

    /**
     * exception handler
     */
    default void exceptionProcessing() {

    }

    /**
     * post-processing logic
     */
    default void postProcessing() {

    }
}
