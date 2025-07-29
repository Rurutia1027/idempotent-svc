package com.cloudnative.apps.http.handler;

import com.cloudnative.apps.http.IdempotentSpELExecuteHandler;
import com.cloudnative.idm.annotation.Idempotent;
import com.cloudnative.idm.aspect.wrapper.AbstractIdempotentWrapper;
import com.cloudnative.idm.context.IdempotentContext;
import com.cloudnative.idm.enums.IdempotentTypeEnum;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IdempotentSpELExecuteHandlerTest {
    private RedissonClient redissonClient;
    private IdempotentSpELExecuteHandler handler;

    @BeforeEach
    void setUp() {
        redissonClient = mock(RedissonClient.class);
        handler = new IdempotentSpELExecuteHandler(redissonClient);
        IdempotentContext.clean();
    }

    @Test
    public void testBuildWrapperWithValidAnnotation() throws NoSuchMethodException {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.ADVICE_EXECUTION);
        Method method = TestService.class.getMethod("testMethod", String.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getParameterNames()).thenReturn(new String[]{"name"});

        when(pjp.getSignature()).thenReturn(signature);
        when(pjp.getArgs()).thenReturn(new Object []{"testUser"});

        AbstractIdempotentWrapper wrapper = handler.buildWrapper(pjp);
        assertNotNull(wrapper);
        assertTrue(wrapper.getLockKey().contains("testUser"));
        assertTrue(wrapper.getLockKey().contains("user:"));
    }

    @Test
    void testHandlerSuccessLockAcquired() {
        AbstractIdempotentWrapper wrapper = mock(AbstractIdempotentWrapper.class);
        when(wrapper.getLockKey()).thenReturn("lock:test:key");
        when(wrapper.getIdempotent()).thenThrow(new RuntimeException("Duplicate"));

        RLock lock = mock(RLock.class);
        when(redissonClient.getLock("lock:test:key")).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);

        assertDoesNotThrow(() -> handler.handler(wrapper));
        assertEquals(lock, IdempotentContext.getKey("lock:http:spel"));
    }

    @Test
    void testHandlerFailsWhenLockNotAcquired() {
        AbstractIdempotentWrapper wrapper = mock(AbstractIdempotentWrapper.class);
        when(wrapper.getLockKey()).thenReturn("lock:fail:key");
        when(wrapper.getIdempotent()).thenThrow(new RuntimeException("Already running"));


        RLock lock = mock(RLock.class);
        when(redissonClient.getLock("lock:fail:key")).thenReturn(lock);
        when(lock.tryLock()).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> handler.handler(wrapper));
        assertEquals("Already running", ex.getMessage());
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
                key = "#name",
                type = IdempotentTypeEnum.SPEL,
                uniqueKeyPrefix = "custom:prefix",
                message = "Duplicate request, please try later"
        )
        public void testMethod(String name) {
            // mocked method
        }
    }
}