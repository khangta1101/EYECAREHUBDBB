package com.example.EyeCareHubDB.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "vnpay")
public class VnPayProperties {
    private String tmnCode;
    private String hashSecret;
    private String payUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private String returnUrl;
    private String version = "2.1.0";
    private String command = "pay";
    private String orderType = "other";
    private String locale = "vn";
}
