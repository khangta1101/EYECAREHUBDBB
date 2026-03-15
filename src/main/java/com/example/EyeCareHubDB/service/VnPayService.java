package com.example.EyeCareHubDB.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.example.EyeCareHubDB.config.VnPayProperties;
import com.example.EyeCareHubDB.entity.Payment;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VnPayService {

    private static final DateTimeFormatter VNPAY_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VnPayProperties vnPayProperties;

    public String buildPaymentUrl(Payment payment, String clientIp, String orderInfo, String customReturnUrl) {
        validateConfig();
        LocalDateTime now = LocalDateTime.now();

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", vnPayProperties.getVersion());
        params.put("vnp_Command", vnPayProperties.getCommand());
        params.put("vnp_TmnCode", vnPayProperties.getTmnCode());
        params.put("vnp_Amount", toVnPayAmount(payment.getAmount()));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", payment.getTransactionRef());
        params.put("vnp_OrderInfo", defaultIfBlank(orderInfo, "Thanh toan don hang " + payment.getId()));
        params.put("vnp_OrderType", vnPayProperties.getOrderType());
        params.put("vnp_Locale", vnPayProperties.getLocale());
        params.put("vnp_ReturnUrl", defaultIfBlank(customReturnUrl, vnPayProperties.getReturnUrl()));
        params.put("vnp_IpAddr", defaultIfBlank(clientIp, "127.0.0.1"));
        params.put("vnp_CreateDate", now.format(VNPAY_TIME_FORMAT));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(VNPAY_TIME_FORMAT));

        String query = buildQueryString(params);
        String secureHash = hmacSha512(vnPayProperties.getHashSecret(), query);
        return vnPayProperties.getPayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    public boolean validateSignature(Map<String, String> queryParams) {
        validateConfig();
        String secureHash = queryParams.get("vnp_SecureHash");
        if (isBlank(secureHash)) {
            return false;
        }

        Map<String, String> signedParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || !key.startsWith("vnp_") || isBlank(value)) {
                continue;
            }
            if ("vnp_SecureHash".equals(key) || "vnp_SecureHashType".equals(key)) {
                continue;
            }
            signedParams.put(key, value);
        }

        String rawData = buildQueryString(signedParams);
        String calculatedHash = hmacSha512(vnPayProperties.getHashSecret(), rawData);
        return calculatedHash.equalsIgnoreCase(secureHash);
    }

    public String serializeResponse(Map<String, String> queryParams) {
        return new TreeMap<>(queryParams).toString();
    }

    private String toVnPayAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than 0 for VNPay payment");
        }
        return amount.multiply(BigDecimal.valueOf(100))
            .setScale(0, RoundingMode.HALF_UP)
            .toPlainString();
    }

    private String buildQueryString(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (isBlank(entry.getValue())) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            builder.append('=');
            builder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return builder.toString();
    }

    private String hmacSha512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(keySpec);
            byte[] hashBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                String part = Integer.toHexString(0xff & b);
                if (part.length() == 1) {
                    hex.append('0');
                }
                hex.append(part);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new RuntimeException("Cannot sign VNPay request", ex);
        }
    }

    private void validateConfig() {
        if (isBlank(vnPayProperties.getTmnCode()) || isBlank(vnPayProperties.getHashSecret()) || isBlank(vnPayProperties.getReturnUrl())) {
            throw new RuntimeException("VNPay configuration is missing. Check vnpay.tmn-code, vnpay.hash-secret and vnpay.return-url");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }
}
