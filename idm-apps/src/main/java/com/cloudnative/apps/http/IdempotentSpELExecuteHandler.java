package com.cloudnative.apps.http;

import cn.hutool.core.util.StrUtil;
import com.cloudnative.idm.annotation.Idempotent;
import com.cloudnative.idm.aspect.handler.AbstractIdempotentExecuteHandler;
import com.cloudnative.idm.aspect.wrapper.AbstractIdempotentWrapper;
import com.cloudnative.idm.aspect.wrapper.IdempotentSpelWrapper;
import com.cloudnative.idm.context.IdempotentContext;
import com.cloudnative.idm.enums.IdempotentTypeEnum;
import com.cloudnative.idm.service.IdempotentSpELHTTPService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

@RequiredArgsConstructor
public class IdempotentSpELExecuteHandler extends AbstractIdempotentExecuteHandler
        implements IdempotentSpELHTTPService {
    private final RedissonClient redissonClient;
    // lock:${idempotent-scene}:${idempotent-type}
    private final static String LOCK = "lock:http:spel";

    @Override
    protected AbstractIdempotentWrapper buildWrapper(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Idempotent idempotent = method.getAnnotation(Idempotent.class);

        if (idempotent == null || idempotent.type() != IdempotentTypeEnum.SPEL) {
            throw new IllegalStateException("Missing or invalid @Idempotent(type=SPEL) " +
                    "annotation");
        }

        String spelKey = idempotent.key();
        String evaluatedKey = parseSpEL(spelKey, joinPoint);
        String lockKey = buildLockKey(idempotent, evaluatedKey);

        return IdempotentSpelWrapper.builder()
                .idempotent(idempotent)
                .joinPoint(joinPoint)
                .spelKey(spelKey)
                .lockKey(lockKey)
                .build();
    }

    public String buildLockKey(Idempotent idempotent, String spelResult) {
        String prefix = StrUtil.blankToDefault(idempotent.uniqueKeyPrefix(), "idempotent:spel");
        return String.format("%s:%s:user:%s",
                prefix,
                spelResult,
                getCurrentUserId());
    }

    public String parseSpEL(String spelKey, ProceedingJoinPoint joinPoint) {
        if (StrUtil.isBlank(spelKey)) {
            return "default";
        }

        Object[] args = joinPoint.getArgs();
        String[] paramNames =
                ((MethodSignature) joinPoint.getSignature()).getParameterNames();

        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < args.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        ExpressionParser parser = new SpelExpressionParser();
        return parser.parseExpression(spelKey).getValue(context, String.class); 
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