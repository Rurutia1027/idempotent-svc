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

/**
 * idempotent parameters wrapper
 */
@Data
@SuperBuilder
@Accessors(chain = true)
public class IdempotentParamWrapper extends AbstractIdempotentWrapper {

}
