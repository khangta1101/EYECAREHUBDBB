package com.example.EyeCareHubDB.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
        params.put("vnp_TmnCode", vnPayProperties.getTmnCode().trim());
        params.put("vnp_Amount", toVnPayAmount(payment.getAmount()));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", payment.getTransactionRef());
        params.put("vnp_OrderInfo", defaultIfBlank(orderInfo, "Thanh toan don hang " + payment.getId()));
        params.put("vnp_OrderType", vnPayProperties.getOrderType());
        params.put("vnp_Locale", vnPayProperties.getLocale());

        String vnpReturnUrl = isBlank(customReturnUrl) ? vnPayProperties.getReturnUrl() : customReturnUrl;
        params.put("vnp_ReturnUrl", vnpReturnUrl);
        params.put("vnp_IpAddr", ipAddr);
        params.put("vnp_CreateDate", now.format(VNPAY_TIME_FORMAT));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(VNPAY_TIME_FORMAT));

        // BUILD HASH DATA & QUERY STRING
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value != null && !value.isBlank()) {
                String encodedKey = encode(key);
                String encodedValue = encode(value);

                hashData.append(key).append("=").append(encodedValue).append("&");
                query.append(encodedKey).append("=").append(encodedValue).append("&");
            }
        }

        // Remove trailing &
        if (hashData.length() > 0) hashData.setLength(hashData.length() - 1);
        if (query.length() > 0) query.setLength(query.length() - 1);

        String secureHash = hmacSha512(vnPayProperties.getHashSecret().trim(), hashData.toString());

        System.out.println("====== VNPAY REQUEST DEBUG ======");
        System.out.println("HASH DATA: [" + hashData + "]");
        System.out.println("SECURE HASH: [" + secureHash + "]");

        String finalUrl = vnPayProperties.getPayUrl() + "?" + query.toString() + "&vnp_SecureHash=" + secureHash;
        System.out.println("FINAL URL: [" + finalUrl + "]");
        return finalUrl;
    }

    // ================= HASH DATA =================
    // Removed redundant buildHashData and buildQuery methods as they are now integrated into buildPaymentUrl for consistency.

    // ================= VALIDATE SIGNATURE =================
    public boolean validateSignature(Map<String, String> params) {
        try {
            String vnpSecureHash = params.get("vnp_SecureHash");
            if (vnpSecureHash == null || vnpSecureHash.isEmpty()) {
                System.out.println("VNPAY VALIDATE: Missing vnp_SecureHash");
                return false;
            }

            // Remove only the hash itself. Keep EVERYTHING else for trying different combinations.
            Map<String, String> filtered = new HashMap<>(params);
            filtered.remove("vnp_SecureHash");

            // We will try combinations:
            // 1. Encoding style: '+' (Standard) vs '%20' (Modern)
            // 2. Inclusion: With 'vnp_SecureHashType' vs Without it.
            
            boolean match = tryValidate(filtered, vnpSecureHash, true, true, "Standard(+), WithHashType");
            if (match) return true;

            match = tryValidate(filtered, vnpSecureHash, true, false, "Standard(+), NoHashType");
            if (match) return true;

            match = tryValidate(filtered, vnpSecureHash, false, true, "Modern(%20), WithHashType");
            if (match) return true;

            match = tryValidate(filtered, vnpSecureHash, false, false, "Modern(%20), NoHashType");
            if (match) return true;

            System.out.println("VNPAY VALIDATE: All combinations failed for signature: " + vnpSecureHash);
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean tryValidate(Map<String, String> params, String expectedHash, boolean usePlus, boolean includeHashType, String label) {
        Map<String, String> filtered = new HashMap<>(params);
        if (!includeHashType) {
            filtered.remove("vnp_SecureHashType");
        }

        List<String> fieldNames = new ArrayList<>(filtered.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String value = filtered.get(fieldName);
            if (value != null && !value.isEmpty() && fieldName.startsWith("vnp_")) {
                // VNPAY 2.1.0: keys are NOT encoded for the HASH DATA (usually vnp_ keys are safe)
                // Values MUST be encoded consistently.
                String encodedValue = customEncode(value, usePlus);
                
                hashData.append(fieldName)
                        .append("=")
                        .append(encodedValue)
                        .append("&");
            }
        }

        if (hashData.length() > 0) {
            hashData.deleteCharAt(hashData.length() - 1);
        }

        String calculatedHash = hmacSha512(vnPayProperties.getHashSecret(), hashData.toString());
        
        System.out.println("VNPAY TRY [" + label + "]:");
        System.out.println("  DATA: [" + hashData + "]");
        System.out.println("  EXPECTED: [" + expectedHash + "]");
        System.out.println("  CALC: [" + calculatedHash + "]");
        
        return calculatedHash.equalsIgnoreCase(expectedHash);
    }

    private String customEncode(String value, boolean usePlus) {
        try {
            // Standard encoder gives + for spaces and lowercase hex (e.g. %2f)
            String encoded = URLEncoder.encode(value, StandardCharsets.UTF_8);
            
            // VNPAY 2.1.0 often expects uppercase hex (e.g. %2F)
            // We use regex to find and uppercase hex codes
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < encoded.length(); i++) {
                char c = encoded.charAt(i);
                if (c == '%' && i + 2 < encoded.length()) {
                    sb.append(c);
                    sb.append(Character.toUpperCase(encoded.charAt(i + 1)));
                    sb.append(Character.toUpperCase(encoded.charAt(i + 2)));
                    i += 2;
                } else {
                    sb.append(c);
                }
            }
            
            String result = sb.toString();
            if (!usePlus) {
                result = result.replace("+", "%20");
            }
            return result;
        } catch (Exception e) {
            return value;
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
            // Standard encoder gives + for spaces and lowercase hex (e.g. %2f)
            String encoded = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
            
            // Uppercase hex codes (e.g. %2f -> %2F) and replace + with %20
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < encoded.length(); i++) {
                char c = encoded.charAt(i);
                if (c == '%' && i + 2 < encoded.length()) {
                    sb.append(c);
                    sb.append(Character.toUpperCase(encoded.charAt(i + 1)));
                    sb.append(Character.toUpperCase(encoded.charAt(i + 2)));
                    i += 2;
                } else if (c == '+') {
                    sb.append("%20");
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
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
                result.append(String.format("%02x", b & 0xff));
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