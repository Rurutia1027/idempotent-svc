package com.cloudnative.idem.core.aspect;

import com.cloudnative.idem.api.annotation.Idempotent;
import com.cloudnative.idem.api.exception.RepeatConsumptionException;
import com.cloudnative.idem.core.context.IdempotentContext;
import com.cloudnative.idem.core.factory.IdempotentExecuteHandlerFactory;
import com.cloudnative.idem.core.handler.IdempotentExecuteHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

@Aspect
public class IdempotentAspect {
    /**
     * Idempotency AOP executor. Executes business logic by scanning for the
     * {@link Idempotent} annotation.
     */
    @Around("@annotation(com.cloudnative.idem.api.annotation.Idempotent)")
    /**
     * Intercepts and processes methods annotated with {@link Idempotent}.
     */
    public Object idempotentHandler(ProceedingJoinPoint joinPoint) throws Throwable {
        Idempotent idempotent = getIdempotent(joinPoint);
        IdempotentExecuteHandler instance =
                IdempotentExecuteHandlerFactory.getInstance(idempotent.scene(),
                        idempotent.type());
        Object retObj;
        try {
            instance.execute(joinPoint, idempotent);
            retObj = joinPoint.proceed();
            instance.postProcessing();
        } catch (RepeatConsumptionException ex) {
            /**
             * Two scenarios when idempotency is triggered:
             * - The Message is still being processed, but it's unclear if it was successful.
             *   In this case, return an error so RocketMQ can retry via the retry queue.
             * - The message was already processed successfully.
             *   In this case, simply return a successful response.
             */
            if (!ex.getError()) {
                return null;
            }
            throw ex;
        } catch (Throwable ex) {
            // An exception occurred during message processing.
            // Clear the idempotency marker to allow RocketMQ to retry via the retry queue.
            instance.exceptionProcessing();
            throw ex;
        } finally {
            // Clear the thread-local context for idempotency context
            IdempotentContext.clean();
        }

        return retObj;
    }

    /**
     * Retrieves the {@link Idempotent} annotation from the target method.
     */
    public static Idempotent getIdempotent(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod =
                joinPoint.getTarget().getClass().getDeclaredMethod(methodSignature.getName(),
                        methodSignature.getMethod().getParameterTypes());
        return targetMethod.getAnnotation(Idempotent.class);
    }
}
