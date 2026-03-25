import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class HashCheck {
    public static void main(String[] args) throws Exception {
        String secret = "9YDIUBGQV3BUX29ZCR8HIA9V2ZE71FAY";
        String data = "vnp_Amount=48000000&vnp_Command=pay&vnp_CreateDate=20260325183309&vnp_CurrCode=VND&vnp_ExpireDate=20260325184809&vnp_IpAddr=210.245.8.19&vnp_Locale=vn&vnp_OrderInfo=ThanhToanDonHang109&vnp_OrderType=other&vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A5173%2Fpayment-result&vnp_TmnCode=TXXKQV1N&vnp_TxnRef=VNP1081774438389953&vnp_Version=2.1.0";
        
        Mac hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac.init(secretKey);
        byte[] hashBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : hashBytes) {
            result.append(String.format("%02x", b & 0xff));
        }
        System.out.println(result.toString().toUpperCase());
    }
}
