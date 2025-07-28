package com.cloudnative.idm.aspect.handler;

import com.cloudnative.idm.annotation.Idempotent;
import com.cloudnative.idm.aspect.wrapper.IdempotentParamWrapper;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Idempotent execution handler interface.
 *
 * <p>
 * This interface define the contract for how to handle idempotent operations in
 * business code. Typically used in conjunction with AOP aspects that intercept
 * annotated methods, extract key information, and apply Redis-based or database-based
 * idempotency protections.
 * <p>
 * Implementations are responsible for checking request uniqueness, caching results, and
 * preventing duplicate method execution in high-concurrency or retry scenarios.
 * </p>
 */
public interface IdempotentExecuteHandler {
    /**
     * Main handler for idempotent processing, invoked by the AOP layer.
     * <p>
     * This method coordinates the full lifecycle of idempotency enforcement:
     *     <ul>
     *         <li>Generate or retrieve the idempotent key (e.g., from headers, arguments)</li>
     *         <li>Check Redis (or other store) to determine whether the request has been
     *         processed.</li>
     *         <li>Attempt to acquire lock for execution</li>
     *         <li>Invoke target method via {@link ProceedingJoinPoint}</li>
     *         <li>Cache the result or error code</li>
     *     </ul>
     * </p>
     *
     * @param wrapper encapsulated method context, includes arguments, idempotent
     *                annotation, Redis key, etc. ¬
     */
    void handler(IdempotentParamWrapper wrapper);


    /**
     * Method execution point, only called after confirming idempotent preconditions.
     * <p>
     * This method executes the actual business logic by invoking {@code joinPoint
     * .proceed()}, and is often surrounded by try-catch-finally blocks in
     * implementation to ensure:
     *     <ul>
     *         <li>Successful result caching</li>
     *         <li>Exception recording and suppression of duplicate retries</li>
     *         <li>Lock release and post cleanup</li>
     *     </ul>
     * </p>
     *
     * @param joinPoint  the AOP method entry point, allows proceeding with original method.
     * @param idempotent the @Idempotent annotation instance extracted from the intercepted
     *                   method.
     */
    void execute(ProceedingJoinPoint joinPoint, Idempotent idempotent);

    /**
     * Optional exception processing hook.
     *
     * <p>
     * Triggered when a downstream exception is caught during execution (e.g., network
     * failure, DB error), and can be used to log, alert, rollback state, or clean up
     * temporary Redis entries if needed.
     * </p>
     */
    default void exceptionProcessing() {
        // Optional: Implement if rollback or alerting is required
    }

    /**
     * Optional post-processing hook.
     * <p>Executed after a successful or failed method call, used for:
     * <ul>
     *     <li>Lock release</li>
     *     <li>Clearing temporary markers</li>
     *     <li>Metrics reporting</li>
     * </ul>
     * </p>
     */
    default void postProcessing() {
        // Optional: Implement for clean-up or monitoring logic
    }
}
