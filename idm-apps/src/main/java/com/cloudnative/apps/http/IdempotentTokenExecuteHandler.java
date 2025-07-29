package com.cloudnative.apps.http;

import com.cloudnative.idm.annotation.Idempotent;
import com.cloudnative.idm.aspect.handler.AbstractIdempotentExecuteHandler;
import com.cloudnative.idm.aspect.wrapper.AbstractIdempotentWrapper;
import com.cloudnative.idm.aspect.wrapper.IdempotentParamWrapper;
import com.cloudnative.idm.aspect.wrapper.IdempotentTokenWrapper;
import com.cloudnative.idm.context.IdempotentContext;
import com.cloudnative.idm.enums.IdempotentTypeEnum;
import com.cloudnative.idm.service.IdempotentTokenService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@RequiredArgsConstructor
public class IdempotentTokenExecuteHandler extends AbstractIdempotentExecuteHandler
        implements IdempotentTokenService {

    private final RedissonClient redissonClient;
    // lock:${idempotent-scene}:${idempotent-type}
    private final static String LOCK = "lock:http:token";


    @Override
    public AbstractIdempotentWrapper buildWrapper(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Idempotent idempotent = method.getAnnotation(Idempotent.class);

        if (idempotent == null || idempotent.type() != IdempotentTypeEnum.TOKEN) {
            throw new IllegalStateException("Missing or invalid @Idempotent(type=SPEL) " +
                    "annotation");
        }

        String token = getRequestToken();
        String lockKey = String.format("idempotent:token:%s", token);
        return IdempotentTokenWrapper.builder().idempotent(idempotent)
                .joinPoint(joinPoint)
                .lockKey(lockKey)
                .requestToken(token)
                .build();
    }

    private String getRequestToken() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs.getRequest();
        return request.getHeader("Idempotency-Key");
    }

    @Override
    public void handler(AbstractIdempotentWrapper wrapper) {
        String lockKey = wrapper.getLockKey();
        RLock lock = redissonClient.getLock(lockKey);
        if (!lock.tryLock()) {
            throw new RuntimeException(wrapper.getIdempotent().message());
        }
        IdempotentContext.put(LOCK, lock);
    }

    @Override
    public void exceptionProcessing() {
        super.exceptionProcessing();
    }

    @Override
    public void postProcessing() {
        super.postProcessing();
    }
}
