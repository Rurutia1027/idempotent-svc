package com.cloudnative.idem.core.factory;

import com.cloudnative.idem.api.enums.IdempotentSceneEnum;
import com.cloudnative.idem.api.enums.IdempotentTypeEnum;
import com.cloudnative.idem.core.context.ApplicationContextHolder;
import com.cloudnative.idem.core.handler.IdempotentExecuteHandler;
import com.cloudnative.idem.core.param.IdempotentParamService;
import com.cloudnative.idem.core.service.IdempotentSpELHTTPService;
import com.cloudnative.idem.core.service.IdempotentSpELMQService;
import com.cloudnative.idem.core.service.IdempotentTokenService;

/**
 * Factory class for producing idempotent execution handlers based on specific scenarios and
 * types.
 */
public final class IdempotentExecuteHandlerFactory {

    /**
     * Returns the appropriate idempotent execution handler based on the specified scenario and type.
     *
     * @param scene The idempotent validation scenario
     * @param type  The idempotent handling strategy
     * @return The corresponding idempotent execution handler
     */
    public static IdempotentExecuteHandler getInstance(IdempotentSceneEnum scene, IdempotentTypeEnum type) {
        IdempotentExecuteHandler result;
        switch (scene) {
            case HTTP:
                switch (type) {
                    case PARAM:
                        result = ApplicationContextHolder.getBean(IdempotentParamService.class);
                        break;
                    case TOKEN:
                        result = ApplicationContextHolder.getBean(IdempotentTokenService.class);
                        break;
                    case SPEL:
                        result =
                                ApplicationContextHolder.getBean(IdempotentSpELHTTPService.class);
                        break;
                    default:
                        throw new RuntimeException(String.format("Unsupported idempotent type: [%s]", type.name()));
                }
                break;
            case MQ:
                result = ApplicationContextHolder.getBean(IdempotentSpELMQService.class);
                break;
            default:
                throw new RuntimeException(String.format("Unsupported idempotent scenario: [%s]", scene.name()));
        }
        return result;
    }
}
