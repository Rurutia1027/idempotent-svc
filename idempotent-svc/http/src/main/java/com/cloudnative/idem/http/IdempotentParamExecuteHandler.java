package com.cloudnative.idem.http;

import com.cloudnative.idem.core.handler.AbstractIdempotentExecuteHandler;
import com.cloudnative.idem.core.param.IdempotentParamWrapper;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.redisson.api.RedissonClient;

@RequiredArgsConstructor
public class IdempotentParamExecuteHandler extends AbstractIdempotentExecuteHandler {
    private final RedissonClient redissionClient;
    private final static String LOCK = "lock:param:restAPI";

    @Override
    protected IdempotentParamWrapper buildWrapper(ProceedingJoinPoint joinPoint) {
        String lockKey = String.format("idempotent:path:%s:currentUserId:%s:md5:%s",
                getServletPath(),
                getCurrentUser(),
                calcArgsMD5(joinPoint));
        return IdempotentParamWrapper.builder().lockKey(lockKey).joinPoint(joinPoint).build();
    }

    private String getServletPath() {
        String ret;
        return ret;
    }



    @Override
    public void handler(IdempotentParamWrapper wrapper) {

    }
}