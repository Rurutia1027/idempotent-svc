package com.cloudnative.idem.api.annotation;

import com.cloudnative.idem.api.enums.IdempotentSceneEnum;
import com.cloudnative.idem.api.enums.IdempotentTypeEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for declaring idempotent operations.
 * <p>
 * This can be applied to HTTP APIs, message consumers, or any service logic that requires
 * idempotency control.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * Idempotency key expression.
     * Only effective when {@link #type()} is set to {@link IdempotentTypeEnum#SPEL}
     */
    String key() default "";

    /**
     * Custom error message returned when a duplicate operation is detected.
     */
    String message() default "Multiply too frequency, please retry later";

    /**
     * Idempotency type to validate against.
     */
    IdempotentTypeEnum type() default IdempotentTypeEnum.PARAM;

    /**
     * Idempotency scenario, determines where this annotation is applied.
     * See {@link IdempotentSceneEnum} for available options.
     */
    IdempotentSceneEnum scene() default IdempotentSceneEnum.HTTP;

    /**
     * Optional prefix for the generated unique key.
     * Effective only when used with {@link IdempotentSceneEnum#MQ} and
     * {@link IdempotentTypeEnum#SPEL}
     */
    String uniqueKeyPrefix() default "";

    /**
     * Expiration time (in seconds) for the idempotency key.
     * Default is 3600 seconds (1 hour).
     * Effective only when used with {@link IdempotentSceneEnum#MQ} and
     * {@link IdempotentTypeEnum#SPEL}
     */
    long keyTimeout() default 3600L;
}
