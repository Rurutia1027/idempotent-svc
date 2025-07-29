package com.cloudnative.apps.http;

import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson2.JSON;
import com.cloudnative.idm.aspect.handler.AbstractIdempotentExecuteHandler;
import com.cloudnative.idm.aspect.wrapper.AbstractIdempotentWrapper;
import com.cloudnative.idm.aspect.wrapper.IdempotentParamWrapper;
import com.cloudnative.idm.context.IdempotentContext;
import com.cloudnative.idm.service.IdempotentParamService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RequiredArgsConstructor
public class IdempotentParamExecuteHandler extends AbstractIdempotentExecuteHandler
        implements IdempotentParamService {
    private final RedissonClient redissionClient;
    // lock:${idempotent-scene}:${idempotent-type}
    private final static String LOCK = "lock:http:param";

    @Override
    public AbstractIdempotentWrapper buildWrapper(ProceedingJoinPoint joinPoint) {
        String lockKey = String.format("idempotent:path:%s:currentUserId:%s:md5:%s",
                getServletPath(),
                getCurrentUserId(),
                genBasedOnMD5Alg(joinPoint));
        return IdempotentParamWrapper.builder().lockKey(lockKey).joinPoint(joinPoint).build();
    }

    private String genBasedOnMD5Alg(ProceedingJoinPoint joinPoint) {
        return DigestUtil.md5Hex(JSON.toJSONBytes(joinPoint.getArgs()));
    }

    private String getServletPath() {
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return servletRequestAttributes.getRequest().getServletPath();
    }

    @Override
    public void handler(AbstractIdempotentWrapper wrapper) {
        String lockKey = wrapper.getLockKey();
        RLock lock = redissionClient.getLock(lockKey);
        if (!lock.tryLock()) {
            throw new RuntimeException(wrapper.getIdempotent().message());
        }
        IdempotentContext.put(LOCK, lock);
    }

    @Override
    public void postProcessing() {
        super.postProcessing();
    }
}