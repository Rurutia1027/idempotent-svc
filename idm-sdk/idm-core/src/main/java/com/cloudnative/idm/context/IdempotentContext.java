package com.cloudnative.idm.context;

import cn.hutool.core.collection.CollUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * IdempotentContext - ThreadLocal-based storage for passing idempotency-related metadata
 * across multiple layers within the same thread of execution.
 *
 * <p>This class acts as a thread-scoped context holder, allowing different components
 * (such as interceptors, AOP aspects, or Redis handlers) to read/write transient metadata
 * during the execution of an idempotent business flow.</p>
 *
 * <p>Typical use cases include:
 * <ul>
 *     <li>Storing the generated idempotency key</li>
 *     <li>Passing token or request header values</li>
 *     <li>Flagging certain conditional states (e.g., retry, lock acquired)</li>
 * </ul></p>
 *
 * <p><strong>Important:</strong> Remember to call {@link #clean()} after request execution
 * (e.g. in a filter or finally block) to avoid memory leaks due to lingering ThreadLocal references.</p>
 */
public final class IdempotentContext {
    /**
     * Thread-local context map, holds request-scoped metadata.
     */
    private static final ThreadLocal<Map<String, Object>> CONTEXT = new ThreadLocal<>();

    /**
     * Retrieve the full context map for the current thread.
     *
     * @return a mutable map containing context data, or null if not initialized.
     */
    public static Map<String, Object> get() {
        return CONTEXT.get();
    }

    /**
     * Retrieve a value from the context by key.
     *
     * @param key the attribute name
     * @return the associated object, or null if not found
     */
    public static Object getKey(String key) {
        Map<String, Object> context = get();
        if (CollUtil.isNotEmpty(context)) {
            return context.get(key);
        }

        return null;
    }

    /**
     * Retrieve a string value from the context by key.
     *
     * @param key the attribute name
     * @return the stringified value, or null if not present
     */
    public static String getString(String key) {
        Object actual = getKey(key);
        if (actual != null) {
            return actual.toString();
        }

        return null;
    }

    /**
     * Add a key-value pair into the thread-local context.
     * Initializes a new map if none exists.
     *
     * @param key the attribute name
     * @param val the attribute value
     */
    public static void put(String key, Object val) {
        Map<String, Object> context = get();
        if (CollUtil.isEmpty(context)) {
            context = new HashMap<>();
        }
        context.put(key, val);
        putContext(context);
    }

    /**
     * Merge or set the full context map into the ThreadLocal store.
     * If a context already exists, it merges entries into it.
     *
     * @param context the new context map to set or merge
     */
    public static void putContext(Map<String, Object> context) {
        Map<String, Object> threadContext = CONTEXT.get();
        if (CollUtil.isNotEmpty(threadContext)) {
            threadContext.putAll(context);
            return;
        }
        CONTEXT.set(context);
    }

    /**
     * Clean the thread-local context to prevent memory leaks.
     * <p>Must be called at the end of a request or method execution.</p>
     */
    public static void clean() {
        CONTEXT.remove();
    }
}
