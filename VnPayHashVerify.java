import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class VnPayHashVerify {
    public static void main(String[] args) throws Exception {
        String secret = "9YDIUBGQV3BUX29ZCR8HIA9V2ZE71FAY";
        
        // Exact hash data from your debug log
        String hashData = "vnp_Amount=48000000&vnp_Command=pay&vnp_CreateDate=20260325190119&vnp_CurrCode=VND&vnp_ExpireDate=20260325191619&vnp_IpAddr=210.245.8.19&vnp_Locale=vn&vnp_OrderInfo=string&vnp_OrderType=other&vnp_ReturnUrl=string&vnp_TmnCode=TXXKQV1N&vnp_TxnRef=VNP1111774440079897&vnp_Version=2.1.0";
        
        String calculated = hmacSha512(secret, hashData);
        
        System.out.println("=== VNPay Hash Verify ===");
        System.out.println("SECRET: [" + secret + "]");
        System.out.println("HASH DATA: [" + hashData + "]");
        System.out.println("CALCULATED HASH: [" + calculated + "]");
        System.out.println("YOUR HASH:       [45DEED733FAAECE05056C4CD72850ADE5DA611A73C9D4FAC843D849D144BC499FB9EB1BA23F69A7AE9E19386EB7621B11B03C42A1E03C688CBFDFC89A9CB18D0]");
        System.out.println("MATCH: " + calculated.equalsIgnoreCase("45DEED733FAAECE05056C4CD72850ADE5DA611A73C9D4FAC843D849D144BC499FB9EB1BA23F69A7AE9E19386EB7621B11B03C42A1E03C688CBFDFC89A9CB18D0"));
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
