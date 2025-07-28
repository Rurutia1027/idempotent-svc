package com.cloudnative.idm.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Exceptions for Repeat Consumption Scenarios.
 * <p>
 * This exception is used to handle idempotency logic in message processing.
 * <p>
 * There are typically two cases when idempotency is triggered:
 * - The message is still being processed, and the outcome is uncertain.
 * In this case, an error should be returned so that RocketMQ can retry delivery.
 * <p>
 * - The message has already been successfully processed.
 * In this case, the message can be acknowledged directly.
 */
@RequiredArgsConstructor
public class RepeatConsumptionException extends RuntimeException {
    /**
     * Error flag:
     * true - indicates processing should be retried.
     * false - indicates message has already been handled successfully
     */
    @Getter
    private final Boolean error;
}
