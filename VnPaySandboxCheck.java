import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;
import java.util.Map;
import java.net.HttpURLConnection;
import java.net.URL;

public class VnPaySandboxCheck {
    public static void main(String[] args) throws Exception {
        String tmnCode = "TXXKQV1N";
        String secret = "9YDIUBGQV3BUX29ZCR8HIA9V2ZE71FAY";
        
        // Build minimal sandbox URL and see if VNPay returns error 70 or something else
        TreeMap<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", "1000000");
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", "TEST001");
        params.put("vnp_OrderInfo", "Thanh toan don hang 1");
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", "http://localhost:5173/payment-result");
        params.put("vnp_IpAddr", "210.245.8.19");
        params.put("vnp_CreateDate", "20260325195500");
        params.put("vnp_ExpireDate", "20260325201000");
        
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String encodedKey = URLEncoder.encode(entry.getKey(), "UTF-8");
            String encodedValue = URLEncoder.encode(entry.getValue(), "UTF-8");
            hashData.append(encodedKey).append("=").append(encodedValue).append("&");
            query.append(encodedKey).append("=").append(encodedValue).append("&");
        }
        hashData.setLength(hashData.length() - 1);
        query.setLength(query.length() - 1);
        
        String hash = hmacSha512(secret, hashData.toString());
        String fullUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?" + query + "&vnp_SecureHash=" + hash;
        
        System.out.println("Hash Data: " + hashData);
        System.out.println("Hash: " + hash);
        System.out.println("Full URL:");
        System.out.println(fullUrl);
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
