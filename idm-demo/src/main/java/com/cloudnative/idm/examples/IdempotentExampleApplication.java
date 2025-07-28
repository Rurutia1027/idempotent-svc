package com.cloudnative.idm.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IdempotentExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdempotentExampleApplication.class, args);
    }
}
