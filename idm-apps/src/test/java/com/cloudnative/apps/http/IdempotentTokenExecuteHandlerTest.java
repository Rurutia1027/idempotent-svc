package com.cloudnative.apps.http;

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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IdempotentTokenExecuteHandlerTest {
    private RedissonClient redissonClient;
    private IdempotentTokenExecuteHandler handler;

    @BeforeEach
    void setUp() {
        redissonClient = mock(RedissonClient.class);
        handler = new IdempotentTokenExecuteHandler(redissonClient);
        IdempotentContext.clean();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Idempotent-Token", "token-xyz-123");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void testBuildWrapperWithValidAnnotation() throws NoSuchMethodException {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Method method = TestService.class.getMethod("submitWithToken", String.class);

        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(pjp.getSignature()).thenReturn(signature);
        when(pjp.getArgs()).thenReturn(new Object[]{"payload"});

        AbstractIdempotentWrapper wrapper = handler.buildWrapper(pjp);
        assertNotNull(wrapper);
        assertEquals("idempotent:token:null", wrapper.getLockKey());
    }

    @Test
    void testHandlerSuccessLockAcquired() {
        AbstractIdempotentWrapper wrapper = mock(AbstractIdempotentWrapper.class);
        when(wrapper.getLockKey()).thenReturn("idempotent:token:token-xyz-123");
        when(wrapper.getIdempotent()).thenThrow(new RuntimeException("Duplicate token"));

        RLock lock = mock(RLock.class);
        when(redissonClient.getLock("idempotent:token:token-xyz-123")).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);

        assertDoesNotThrow(() -> handler.handler(wrapper));
        assertEquals(lock, IdempotentContext.getKey("lock:http:token"));
    }

    @Test
    void testHandlerFailsWhenLockNotAcquired() {
        AbstractIdempotentWrapper wrapper = mock(AbstractIdempotentWrapper.class);
        when(wrapper.getLockKey()).thenReturn("idempotent:token:token-xyz-123");
        when(wrapper.getIdempotent()).thenThrow(new RuntimeException("Already used"));

        RLock lock = mock(RLock.class);
        when(redissonClient.getLock("idempotent:token:token-xyz-123")).thenReturn(lock);
        when(lock.tryLock()).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> handler.handler(wrapper));
        assertEquals("Already used", ex.getMessage());
    }

    // Simulated service with method annotation
    static class TestService {
        @Idempotent(
                type = IdempotentTypeEnum.TOKEN,
                message = "Duplicate token submission"
        )
        public void submitWithToken(String payload) {
            // mocked logic
        }
    }
}