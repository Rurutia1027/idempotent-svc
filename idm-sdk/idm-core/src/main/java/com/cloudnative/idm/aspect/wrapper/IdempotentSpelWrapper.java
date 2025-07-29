package com.cloudnative.idm.aspect.wrapper;

import com.cloudnative.idm.annotation.Idempotent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Wrapper for SpEL-based idempotency
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class IdempotentSpelWrapper extends AbstractIdempotentWrapper{

    /**
     * Original SpEL expression
     */
    private String spelKey;
}
