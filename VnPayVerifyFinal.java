import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class VnPayVerifyFinal {
    public static void main(String[] args) throws Exception {
        String secret = "4L6AEK3UKH4S5ZR3J4349A9PQS9NQYVI";
        String query = "vnp_Amount=48000000&vnp_Command=pay&vnp_CreateDate=20260319021945&vnp_CurrCode=VND&vnp_ExpireDate=20260319023445&vnp_IpAddr=210.245.8.19&vnp_Locale=vn&vnp_OrderInfo=ThanhToanDonHang40&vnp_OrderType=other&vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A5173%2Fpayment-result&vnp_TmnCode=TXXKQV1N&vnp_TxnRef=VNP391773861585693&vnp_Version=2.1.0";
        
        String calculated = hmacSha512(secret, query);
        System.out.println("Calculated: " + calculated);
        
        String expected = "84EDF2B9032527E33B477490F405151FE67698D6BAB532D8914E93BCB4B828B65869DCE3F60E7B80623ABA06F50DA18DA0A562C24028D44A34E89DEC93F39B27";
        System.out.println("Expected:   " + expected);
        
        if (calculated.equalsIgnoreCase(expected)) {
            System.out.println("MATCH!");
        } else {
            System.out.println("NO MATCH");
        }
    }

    private static String hmacSha512(String key, String data) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac.init(secretKey);
        byte[] hashBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : hashBytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString().toUpperCase();
    }
}
