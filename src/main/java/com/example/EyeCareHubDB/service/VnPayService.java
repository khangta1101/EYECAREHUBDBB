package com.example.EyeCareHubDB.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.example.EyeCareHubDB.config.VnPayProperties;
import com.example.EyeCareHubDB.entity.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VnPayService {

    private static final DateTimeFormatter VNPAY_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VnPayProperties vnPayProperties;

    // ================= CREATE PAYMENT URL =================
    public String buildPaymentUrl(Payment payment, String clientIp, String orderInfo, String customReturnUrl) {
        validateConfig();

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        String ipAddr = clientIp;
        if (isBlank(ipAddr) || "127.0.0.1".equals(ipAddr) || "::1".equals(ipAddr)) {
            ipAddr = "210.245.8.19";
        }

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
        params.put("vnp_BankCode", "NCB");

        // ⚠️ luôn dùng config để tránh lệch hash
        params.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl());

        params.put("vnp_IpAddr", ipAddr);
        params.put("vnp_CreateDate", now.format(VNPAY_TIME_FORMAT));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(VNPAY_TIME_FORMAT));

        String hashData = buildHashData(params);
        String query = buildQuery(params);

        String secureHash = hmacSha512(vnPayProperties.getHashSecret(), hashData);

        return vnPayProperties.getPayUrl()
                + "?" + query
                + "&vnp_SecureHashType=SHA512"
                + "&vnp_SecureHash=" + secureHash;
    }

    // ================= HASH DATA =================
    private String buildHashData(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (isBlank(entry.getValue()))
                continue;

            if (sb.length() > 0)
                sb.append('&');

            sb.append(entry.getKey());
            sb.append('=');
            sb.append(encode(entry.getValue())); // ✅ QUAN TRỌNG
        }

        return sb.toString();
    }

    // ================= QUERY =================
    private String buildQuery(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (isBlank(entry.getValue()))
                continue;

            if (sb.length() > 0)
                sb.append('&');

            sb.append(entry.getKey());
            sb.append('=');
            sb.append(encode(entry.getValue()));
        }

        return sb.toString();
    }

    // ================= VALIDATE SIGNATURE =================
    public boolean validateSignature(Map<String, String> params) {
        try {
            String vnpSecureHash = params.get("vnp_SecureHash");

            if (vnpSecureHash == null || vnpSecureHash.isEmpty()) {
                return false;
            }

            // ✅ Bỏ hash ra khỏi params
            Map<String, String> filtered = new HashMap<>(params);
            filtered.remove("vnp_SecureHash");
            filtered.remove("vnp_SecureHashType");

            // ✅ Sort key
            List<String> fieldNames = new ArrayList<>(filtered.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();

            for (String fieldName : fieldNames) {
                String value = filtered.get(fieldName);

                if (value != null && !value.isEmpty()) {
                    hashData.append(fieldName);
                    hashData.append("=");
                    hashData.append(value);
                    hashData.append("&");
                }
            }

            // Xóa dấu & cuối
            hashData.deleteCharAt(hashData.length() - 1);

            String calculatedHash = hmacSHA512(secretKey, hashData.toString());

            // 🔥 DEBUG
            System.out.println("====== DEBUG VNPAY ======");
            System.out.println("HASH DATA: " + hashData);
            System.out.println("CALC HASH: " + calculatedHash);
            System.out.println("VNP HASH: " + vnpSecureHash);

            return calculatedHash.equalsIgnoreCase(vnpSecureHash);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= SERIALIZE =================
    public String serializeResponse(Map<String, String> params) {
        try {
            return new ObjectMapper().writeValueAsString(params);
        } catch (Exception e) {
            return "{}";
        }
    }

    // ================= ENCODE =================
    private String encode(String value) {
        try {
            String encoded = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());

            return encoded.replace("+", "%20");

        } catch (Exception e) {
            return value;
        }
    }

    // ================= HASH =================
    private String hmacSha512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKey);

            byte[] hashBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder();
            for (byte b : hashBytes) {
                result.append(String.format("%02x", b));
            }

            return result.toString().toUpperCase();

        } catch (Exception e) {
            throw new RuntimeException("Hash error", e);
        }
    }

    private String toVnPayAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .toPlainString();
    }

    private void validateConfig() {
        if (isBlank(vnPayProperties.getTmnCode()))
            throw new RuntimeException("Missing tmnCode");
        if (isBlank(vnPayProperties.getHashSecret()))
            throw new RuntimeException("Missing hashSecret");
        if (isBlank(vnPayProperties.getReturnUrl()))
            throw new RuntimeException("Missing returnUrl");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String def) {
        return isBlank(value) ? def : value;
    }
}