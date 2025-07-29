package com.cloudnative.idm.aspect.wrapper;

import com.cloudnative.idm.annotation.Idempotent;
import com.cloudnative.idm.enums.IdempotentTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.aspectj.lang.ProceedingJoinPoint;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public abstract class AbstractIdempotentWrapper {
    /**
     * idempotent annotation
     */
    public Idempotent idempotent;

    /**
     * AOP proceeding join point
     */
    public ProceedingJoinPoint joinPoint;

    /**
     * Lock {@link IdempotentTypeEnum}
     */
    public String lockKey;
}
