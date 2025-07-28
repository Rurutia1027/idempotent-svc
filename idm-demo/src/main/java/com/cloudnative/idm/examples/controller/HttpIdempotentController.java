package com.cloudnative.idm.examples.controller;

import com.cloudnative.idm.annotation.Idempotent;
import com.cloudnative.idm.enums.IdempotentSceneEnum;
import com.cloudnative.idm.enums.IdempotentTypeEnum;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP idempotent endpoint
 */
@RestController
public class HttpIdempotentController {
    @SneakyThrows
    @GetMapping("/idempotent/http/request")
    @Idempotent(
            scene = IdempotentSceneEnum.HTTP,
            type = IdempotentTypeEnum.PARAM,
            message = "idempotency protection, please try later"
    )
    public String idempotentHttpRequest(@RequestParam("orderSn") String orderSn) {
        Thread.sleep(10000);
        System.out.printf("[%s] curren thread idempotency verification %n",
                Thread.currentThread().getName());
        return "success";
    }
}
