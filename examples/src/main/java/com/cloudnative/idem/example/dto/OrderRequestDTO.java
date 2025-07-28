package com.cloudnative.idem.example.dto;

import lombok.Data;

@Data
public class OrderRequestDTO {
    private String username;
    private String orderSn;
}
