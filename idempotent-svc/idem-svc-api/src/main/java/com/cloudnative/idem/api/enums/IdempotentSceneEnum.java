package com.cloudnative.idem.api.enums;

/**
 * Idempotency verify scenario enumerations.
 */
public enum IdempotentSceneEnum {
    /**
     * RESTful API based
     */
    HTTP,
    /**
     * Message Queue based
     */
    MQ
}
