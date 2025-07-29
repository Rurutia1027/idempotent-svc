package com.cloudnative.apps.http;

import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson2.JSON;
import com.cloudnative.idm.annotation.Idempotent;
import com.cloudnative.idm.aspect.wrapper.IdempotentParamWrapper;
import com.cloudnative.idm.enums.IdempotentTypeEnum;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        when(joinPoint.getArgs()).thenReturn(args);

        IdempotentParamWrapper wrapper = (IdempotentParamWrapper) handler.buildWrapper(joinPoint);

        String expectedMd5 = DigestUtil.md5Hex(JSON.toJSONBytes(args));
        String expectedKey = "idempotent:path:/test/path:currentUserId:anonymous:md5:" + expectedMd5;

        assertEquals(expectedKey, wrapper.getLockKey());
        assertEquals(joinPoint, wrapper.getJoinPoint());
    }

    @Test
    void testHandler_AcquiresLockSuccessfully() {
        RLock lock = mock(RLock.class);
        when(lock.tryLock()).thenReturn(true);
        when(redissonClient.getLock(anyString())).thenReturn(lock);

        Idempotent annotation = mock(Idempotent.class);
        when(annotation.message()).thenReturn("Duplicate request");

        IdempotentParamWrapper wrapper = IdempotentParamWrapper.builder()
                .lockKey("test-lock-key")
                .idempotent(annotation)
                .build();

        assertDoesNotThrow(() -> handler.handler(wrapper));
    }

    @Test
    void testHandler_ThrowsExceptionWhenLockFails() {
        RLock lock = mock(RLock.class);
        when(lock.tryLock()).thenReturn(false);
        when(redissonClient.getLock(anyString())).thenReturn(lock);

        Idempotent annotation = mock(Idempotent.class);
        when(annotation.message()).thenReturn("Too frequent");

        IdempotentParamWrapper wrapper = IdempotentParamWrapper.builder()
                .lockKey("test-fail-lock")
                .idempotent(annotation)
                .build();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> handler.handler(wrapper));
        assertEquals("Too frequent", ex.getMessage());
    }

    @Test
    void testReflectionBasedMethodSignatureParsing() throws NoSuchMethodException {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Method method = TestService.class.getMethod("process", String.class, int.class);
        MethodSignature signature = mock(MethodSignature.class);

        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"hello", 42});

        IdempotentParamWrapper wrapper = (IdempotentParamWrapper) handler.buildWrapper(joinPoint);
        assertTrue(wrapper.getLockKey().contains("/test/path"));
        assertTrue(wrapper.getLockKey().contains("anonymous"));
    }


    /**
     * TestService.process(...)   <-- method is annotated
     * ↓
     * [Intercepted by AOP Aspect]   <-- this is where Spring AOP kicks in
     * ↓
     * AbstractIdempotentExecuteHandler
     * → buildWrapper(joinPoint)     <-- wrapper gets created
     * → handler(wrapper)            <-- handler logic runs (lock, dedupe)
     * → proceed with actual method
     * → postProcessing() / exceptionProcessing()
     */
    static class TestService {
        @Idempotent(
                type = IdempotentTypeEnum.PARAM,
                message = "Already submitted"
        )
        public void process(String name, int count) {
            // This is just for annotation reflection testing
        }
    }
}