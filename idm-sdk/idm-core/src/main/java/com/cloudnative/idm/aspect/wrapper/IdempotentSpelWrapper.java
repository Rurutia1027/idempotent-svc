package com.cloudnative.idm.aspect.wrapper;

import com.cloudnative.idm.annotation.Idempotent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Wrapper for SpEL-based idempotency
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class IdempotentSpelWrapper {
    /**
     * Idempotent annotation
     */
    private Idempotent idempotent;

    /**
     * AOP join point
     */
    private ProceedingJoinPoint joinPoint;

    /**
     * Computed lock key from SpEL
     */
    private String lockKey;

    /**
     * Original SpEL expression
     */
    private String spelKey;
}
