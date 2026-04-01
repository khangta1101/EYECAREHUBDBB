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

import java.io.FileWriter;
import java.io.IOException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.example.EyeCareHubDB.config.VnPayProperties;
import com.example.EyeCareHubDB.entity.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

// ============================================================
// SERVICE: VnPayService — Xử lý kỹ thuật tich hợp cổng thanh toán VNPay.
// buildPaymentUrl()  → Tạo URL thanh toán có chữ ký HMAC-SHA512
// validateSignature() → Xác minh chữ ký callback từ VNPay (chống giả mạo)
// Chuẩn: VNPay 2.1.0. Mã hóa: URLEncode %20 cho khoảng trắng, HEX HOA.
// ============================================================
@Service
@RequiredArgsConstructor
public class VnPayService {

    private static final DateTimeFormatter VNPAY_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VnPayProperties vnPayProperties;

    // ================= CREATE PAYMENT URL =================
    // Tạo URL thanh toán VNPay. Các params sắp xếp theo TreeMap (alphabetical).
    // ⭐ Chữ ký HMAC-SHA512: hash toàn bộ params (key=value) sau khi URL-encode.
    // IP "127.0.0.1" hoặc "::1" → tự thay bằng IP thực để VNPay chấp nhận.
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

                // VNPay 2.1.0 OFFICIAL SPEC (from PHP sample):
                // hashData uses urlencode(key) + "=" + urlencode(value)
                // query string also uses encoded key and encoded value
                hashData.append(encodedKey).append("=").append(encodedValue).append("&");
                query.append(encodedKey).append("=").append(encodedValue).append("&");
            }
        }

        // Remove trailing &
        if (hashData.length() > 0)
            hashData.setLength(hashData.length() - 1);
        if (query.length() > 0)
            query.setLength(query.length() - 1);

        String secureHash = hmacSha512(vnPayProperties.getHashSecret().trim(), hashData.toString());

        System.out.println("====== VNPAY REQUEST DEBUG ======");
        System.out.println("TMN CODE: [" + vnPayProperties.getTmnCode().trim() + "]");
        System.out.println("HASH DATA: [" + hashData + "]");
        System.out.println("SECURE HASH: [" + secureHash + "]");

        // Log to file for analysis
        try (FileWriter fw = new FileWriter("C:/Users/ACER/vnpay_debug.log", true)) {
            fw.write("\n====== VNPAY REQUEST DEBUG " + now.toString() + " ======\n");
            fw.write("TMN CODE: [" + vnPayProperties.getTmnCode().trim() + "]\n");
            fw.write("HASH DATA: [" + hashData + "]\n");
            fw.write("SECURE HASH: [" + secureHash + "]\n");
            fw.write("FINAL URL: [" + vnPayProperties.getPayUrl() + "?" + query.toString() + "&vnp_SecureHash="
                    + secureHash + "]\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String finalUrl = vnPayProperties.getPayUrl() + "?" + query.toString() + "&vnp_SecureHash=" + secureHash;
        return finalUrl;
    }

    // ================= HASH DATA =================
    // Removed redundant buildHashData and buildQuery methods as they are now
    // integrated into buildPaymentUrl for consistency.

    // ================= VALIDATE SIGNATURE =================
    // Xác minh chữ ký từ VNPay callback. Loại bỏ vnp_SecureHash rồi tính lại và so sánh.
    // Nếu chữ ký sai → callback có thể bị giả mạo, từ chối xử lý.
    public boolean validateSignature(Map<String, String> params) {
        try {
            String vnpSecureHash = params.get("vnp_SecureHash");
            if (vnpSecureHash == null || vnpSecureHash.isEmpty()) {
                System.out.println("VNPAY VALIDATE: Missing vnp_SecureHash");
                return false;
            }

            // Remove only the hash itself. Keep EVERYTHING else for trying different
            // combinations.
            Map<String, String> filtered = new HashMap<>(params);
            filtered.remove("vnp_SecureHash");

            // Standard VNPay 2.1.0 only needs one attempt with correct encoding
            boolean match = tryValidate(filtered, vnpSecureHash, "Standard 2.1.0");
            if (match)
                return true;

            // Try with vnp_SecureHashType if it exists in params (though not recommended)
            if (params.containsKey("vnp_SecureHashType")) {
                match = tryValidate(new HashMap<>(params), vnpSecureHash, "With HashType");
                if (match)
                    return true;
            }

            System.out.println("VNPAY VALIDATE: All combinations failed for signature: " + vnpSecureHash);
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean tryValidate(Map<String, String> params, String expectedHash, String label) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String value = params.get(fieldName);
            if (value != null && !value.isEmpty() && fieldName.startsWith("vnp_")) {
                // VNPay 2.1.0 OFFICIAL SPEC: urlencode(key) + "=" + urlencode(value)
                hashData.append(encode(fieldName))
                        .append("=")
                        .append(encode(value))
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

    // ================= SERIALIZE =================
    public String serializeResponse(Map<String, String> params) {
        try {
            return new ObjectMapper().writeValueAsString(params);
        } catch (Exception e) {
            return "{}";
        }
    }

    // ================= ENCODE =================
    // URLEncode theo chuẩn VNPay 2.1.0: '+' → '%20', hex HEX HOA (ví dụ: %2F chứ không %2f)
    private String encode(String value) {
        try {
            if (value == null)
                return "";
            // URLEncoder.encode produces '+' for spaces and lowercase hex (e.g. %2f)
            String encoded = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());

            // VNPAY 2.1.0 requires %20 for spaces and UPPERCASE hex encoding
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < encoded.length(); i++) {
                char c = encoded.charAt(i);
                if (c == '+') {
                    sb.append("%20");
                } else if (c == '%' && i + 2 < encoded.length()) {
                    sb.append(c);
                    sb.append(Character.toUpperCase(encoded.charAt(i + 1)));
                    sb.append(Character.toUpperCase(encoded.charAt(i + 2)));
                    i += 2;
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
    // HMAC-SHA512: mã hóa key + data, trả về chuỗi hex HEX HOA (64 byte = 128 ký tự)
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