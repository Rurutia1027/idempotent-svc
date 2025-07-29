package com.cloudnative.idm.examples.dto;

import lombok.Data;

@Data
public class OrderRequestDTO {
    String userId;
    String orderId;
    Long timestamp;
}
