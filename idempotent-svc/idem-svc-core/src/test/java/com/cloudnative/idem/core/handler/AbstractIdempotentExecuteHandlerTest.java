package com.cloudnative.idem.core.handler;

import com.cloudnative.idem.api.annotation.Idempotent;
import com.cloudnative.idem.core.param.IdempotentParamWrapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbstractIdempotentExecuteHandlerTest {
    @Mock
    ProceedingJoinPoint joinPoint;

    @Mock
    Idempotent idempotent;

    @Mock
    IdempotentParamWrapper wrapper;

    @InjectMocks
    TestIdempotentHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = spy(new TestIdempotentHandler(wrapper));
    }

    @Test
    void testExecute_shouldCallBuildWrapper_andHandler() {
        // Arrange
        when(wrapper.setIdempotent(idempotent)).thenReturn(wrapper);

        TestIdempotentHandler spyHandler = spy(handler);

        // Act
        spyHandler.execute(joinPoint, idempotent);

        // Assert here
        verify(spyHandler).buildWrapper(joinPoint);
        verify(wrapper).setIdempotent(idempotent);
        verify(spyHandler).handler(wrapper);
    }

    static class TestIdempotentHandler extends AbstractIdempotentExecuteHandler {
        private final IdempotentParamWrapper wrapper;

        public TestIdempotentHandler(IdempotentParamWrapper wrapper) {
            this.wrapper = wrapper;
        }

        @Override
        protected IdempotentParamWrapper buildWrapper(ProceedingJoinPoint joinPoint) {
            return this.wrapper;
        }

        @Override
        public void handler(IdempotentParamWrapper wrapper) {
            // do nothing
        }
    }

}