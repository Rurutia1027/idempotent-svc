package com.cloudnative.idm.aspect.wrapper;

import com.cloudnative.idm.annotation.Idempotent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.aspectj.lang.ProceedingJoinPoint;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class IdempotentTokenWrapper {
    /**
     * Idempotent annotation metadata
     */
    private Idempotent idempotent;

    /**
     * Join point for AOP interception
     */
    private ProceedingJoinPoint joinPoint;

    /**
     * Lock key built using a token (e.g., from header or body).
     * @see com.cloudnative.idm.enums.IdempotentTypeEnum#TOKEN
     */
    private String lockKey;

    /**
     * Raw token string extracted from request
     */
    private String requestToken;
}
