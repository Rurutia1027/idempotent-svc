package com.cloudnative.idm.examples.controller;

import com.cloudnative.idm.annotation.Idempotent;
import com.cloudnative.idm.enums.IdempotentSceneEnum;
import com.cloudnative.idm.enums.IdempotentTypeEnum;
import com.cloudnative.idm.examples.dto.OrderRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demostrates multiple HTTP idempotency strategies.
 * - Param-based
 * - Token-based
 * - SpEL-based (single + composite request)
 */
@Slf4j
@RestController
@RequestMapping("/idempotent/v1/http")
public class IdempotentHttpController {
    /**
     * Idempotency via HTTP parameter (`userId`)
     *
     * Test cmd:
     * curl -X GET "http://localhost:8080/idempotent/v1/http/param?userId=12345"
     */
    @GetMapping("/param")
    @Idempotent(
            scene = IdempotentSceneEnum.HTTP,
            type = IdempotentTypeEnum.PARAM,
            message = "Duplicate request"
    )
    public String paramBased(@RequestParam("userId") String userId) {
        log.info("Processing param-based idempotent request for userId={}", userId);
        return "Handled idempotent via param";
    }

    /**
     * Idempotency via Token in header or param (e.g., X-Idempotency-Token)
     *
     * Test cmd:
     * curl -X POST "http://localhost:8080/idempotent/v1/http/token" \
     *   -H "X-Idempotency-Token: abc123" \
     *   -H "Content-Type: application/json"
     */
    @PostMapping("/token")
    @Idempotent(
            scene = IdempotentSceneEnum.HTTP,
            type = IdempotentTypeEnum.TOKEN,
            message = "Duplicate submission detected by token"
    )
    public String tokenBased(@RequestHeader(value = "X-Idempotency-Token", required = false) String token) {
        log.info("Processing token-based idempotent request with token={}", token);
        return "Handled via token";
    }

    /**
     * Idempotency via SpEL expression on a single DTO property (`orderId`)
     *
     * Test cmd:
     * curl -X POST "http://localhost:8080/idempotent/v1/http/spel/single" \
     *   -H "Content-Type: application/json" \
     *   -d '{"orderId": "ORD123"}'
     */
    @PostMapping("/spel/single")
    @Idempotent(
            scene = IdempotentSceneEnum.HTTP,
            type = IdempotentTypeEnum.SPEL,
            key = "#order.orderId",
            message = "Duplicate order request"
    )
    public String spelString(@RequestBody OrderRequestDTO order) {
        log.info("Processing SpEL (single) idempotent request for orderId={}",
                order.getOrderId());
        return "Handled via SpEL (single field)";
    }

    /**
     * Idempotency via composite SpEL key (e.g., orderId + userId)
     *
     * Test cmd:
     * curl -X POST "http://localhost:8080/idempotent/v1/http/spel/composite" \
     *   -H "Content-Type: application/json" \
     *   -d '{"orderId": "ORD123", "userId": "user789"}'
     */
    @PostMapping("/spel/composite")
    @Idempotent(
            scene = IdempotentSceneEnum.HTTP,
            type = IdempotentTypeEnum.SPEL,
            key = "#order.orderId + '_' + #order.userId",
            message = "Duplicate compose order request"
    )
    public String spelComposite(@RequestBody OrderRequestDTO order) {
        log.info("Processing SpEL (composite) idempotent request for orderId={} and " +
                        "userId={}", order.getOrderId(), order.getUserId());
        return "Handled via SpEL (composite)";
    }
}
