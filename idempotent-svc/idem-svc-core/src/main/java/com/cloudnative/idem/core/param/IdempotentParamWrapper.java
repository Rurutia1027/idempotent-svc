package com.cloudnative.idem.core.param;

import com.cloudnative.idem.api.annotation.Idempotent;
import com.cloudnative.idem.api.enums.IdempotentTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * idempotent parameters wrapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class IdempotentParamWrapper {
    /**
     * idempotent annotation
     */
    private Idempotent idempotent;

    /**
     * AOP proceeding join point
     */
    private ProceedingJoinPoint joinPoint;

    /**
     * Lock {@link IdempotentTypeEnum#PARAM}
     */
    private String lockKey;
}
