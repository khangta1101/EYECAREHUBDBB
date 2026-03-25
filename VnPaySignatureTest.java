import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VnPaySignatureTest {
    public static void main(String[] args) throws Exception {
        String tmnCode = "TXXKQV1N";
        String hashSecret = "9YDIUBGQV3BUX29ZCR8HIA9V2ZE71FAY";
        
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", "1000000"); // 10,000 VND
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", "TEST12345");
        params.put("vnp_OrderInfo", "Thanh toan don hang 1");
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", "http://localhost:5173/payment-result");
        params.put("vnp_IpAddr", "210.245.8.19");
        params.put("vnp_CreateDate", "20260325185500");
        params.put("vnp_ExpireDate", "20260325191000");

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value != null && !value.isBlank()) {
                String encodedValue = encode(value);
                hashData.append(key).append("=").append(encodedValue).append("&");
                query.append(encode(key)).append("=").append(encodedValue).append("&");
            }
        }

        if (hashData.length() > 0) hashData.setLength(hashData.length() - 1);
        if (query.length() > 0) query.setLength(query.length() - 1);

        String secureHash = hmacSha512(hashSecret, hashData.toString());
        
        System.out.println("HASH DATA: " + hashData.toString());
        System.out.println("SECURE HASH: " + secureHash);
        System.out.println("URL: https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?" + query.toString() + "&vnp_SecureHash=" + secureHash);
    }

    private static String encode(String value) throws Exception {
        String encoded = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
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
    }

    private static String hmacSha512(String key, String data) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac.init(secretKey);
        byte[] hashBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : hashBytes) {
            result.append(String.format("%02x", b & 0xff));
        }
        return result.toString().toUpperCase();
    }
}
