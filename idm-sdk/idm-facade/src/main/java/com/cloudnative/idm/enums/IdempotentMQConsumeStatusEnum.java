package com.cloudnative.idm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * Enum representing the message consumption status used in message queue-based idempotency
 * control.
 */
@RequiredArgsConstructor
public enum IdempotentMQConsumeStatusEnum {
    /**
     * The message is currently being consumed (processing in progress).
     */
    CONSUMING("0"),

    /**
     * The message has already been successfully consumed.
     */
    CONSUMED("1");

    @Getter
    private final String code;

    /**
     * Determines whether the given consumption status represents an in-progress state,
     * which is considered an error in the context of idempotency (i.e., not safe to process
     * again).
     *
     * @param consumeStatus The status code to evaluate.
     * @return true if the status indicates the message is still being processed; false
     * otherwise.
     */
    public static boolean isError(String consumeStatus) {
        return Objects.equals(CONSUMING.code, consumeStatus);
    }
}
