package com.cloudnative.idm.enums;

/**
 * Idempotent verification type enumeration.
 */
public enum IdempotentTypeEnum {
    /**
     * Token based verification.
     */
    TOKEN,

    /**
     * Method Parameter based verification.
     */
    PARAM,

    /**
     * SpEL expression base verification.
     */
    SPEL
}
