package com.cloudnative.apps.http;

import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson2.JSON;
import com.cloudnative.idm.annotation.Idempotent;
import com.cloudnative.idm.aspect.wrapper.IdempotentParamWrapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

public class IdempotentParamExecuteHandlerTest {
    private RedissonClient redissonClient;
    private IdempotentParamExecuteHandler handler;

    @BeforeEach
    void setUp() {
        redissonClient = mock(RedissonClient.class);
        handler = new IdempotentParamExecuteHandler(redissonClient);

        // Mock current user
        TestingAuthenticationToken auth = new TestingAuthenticationToken("user123", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Mock servlet path
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/test/path");
        RequestAttributes attrs = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attrs);
    }

    @Test
    void testBuildWrapper_GeneratesCorrectLockKey() {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Object[] args = new Object[]{"foo", 123};
        Mockito.when(joinPoint.getArgs()).thenReturn(args);

        IdempotentParamWrapper wrapper = (IdempotentParamWrapper) handler.buildWrapper(joinPoint);

        String expectedMd5 = DigestUtil.md5Hex(JSON.toJSONBytes(args));
        String expectedKey = "idempotent:path:/test/path:currentUserId:anonymous:md5:" + expectedMd5;

        assertEquals(expectedKey, wrapper.getLockKey());
        assertEquals(joinPoint, wrapper.getJoinPoint());
    }

    @Test
    void testHandler_AcquiresLockSuccessfully() {
        RLock lock = mock(RLock.class);
        Mockito.when(lock.tryLock()).thenReturn(true);
        Mockito.when(redissonClient.getLock(anyString())).thenReturn(lock);

        Idempotent annotation = mock(Idempotent.class);
        Mockito.when(annotation.message()).thenReturn("Duplicate request");

        IdempotentParamWrapper wrapper = IdempotentParamWrapper.builder()
                .lockKey("test-lock-key")
                .idempotent(annotation)
                .build();

        assertDoesNotThrow(() -> handler.handler(wrapper));
    }

    @Test
    void testHandler_ThrowsExceptionWhenLockFails() {
        RLock lock = mock(RLock.class);
        Mockito.when(lock.tryLock()).thenReturn(false);
        Mockito.when(redissonClient.getLock(anyString())).thenReturn(lock);

        Idempotent annotation = mock(Idempotent.class);
        Mockito.when(annotation.message()).thenReturn("Too frequent");

        IdempotentParamWrapper wrapper = IdempotentParamWrapper.builder()
                .lockKey("test-fail-lock")
                .idempotent(annotation)
                .build();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> handler.handler(wrapper));
        assertEquals("Too frequent", ex.getMessage());
    }
}