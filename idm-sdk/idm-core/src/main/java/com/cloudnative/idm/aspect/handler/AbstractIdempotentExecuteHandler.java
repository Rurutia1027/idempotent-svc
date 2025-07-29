package com.cloudnative.idm.aspect.handler;

import com.cloudnative.idm.annotation.Idempotent;
import com.cloudnative.idm.aspect.wrapper.AbstractIdempotentWrapper;
import com.cloudnative.idm.aspect.wrapper.IdempotentParamWrapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Abstract class for idempotent execute handler.
 */
public abstract class AbstractIdempotentExecuteHandler implements IdempotentExecuteHandler {
    /**
     * Construct parameter wrapper instance required during idempotent verification.
     *
     * @param joinPoint AOP method processor
     * @return idempotent parameter wrapper
     */
    public abstract AbstractIdempotentWrapper buildWrapper(ProceedingJoinPoint joinPoint);

    /**
     * Idempotent execute entry point.
     *
     * @param joinPoint  AOP join point instance.
     * @param idempotent idempotent annotation
     */
    public void execute(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        AbstractIdempotentWrapper idempotentParamWrapper =
                buildWrapper(joinPoint).setIdempotent(idempotent);
        handler(idempotentParamWrapper);
    }


    protected String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth == null || !auth.isAuthenticated()) ? "anonymous" : auth.getName();
    }
}
