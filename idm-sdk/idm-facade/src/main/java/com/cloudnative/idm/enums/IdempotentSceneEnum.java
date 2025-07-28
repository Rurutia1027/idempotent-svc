package com.cloudnative.idm.enums;

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
