package org.example.websitesalephone.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Getter
@Configuration
public class PayOSConfig {

    @Value("${payos.clientId}")
    private String clientId;

    @Value("${payos.apiKey}")
    private String apiKey;

    @Value("${payos.checksumKey}")
    private String checksumKey;

    @Value("${payos.returnUrl}")
    private String returnUrl;

    @Value("${payos.cancelUrl}")
    private String cancelUrl;

    public static String hmacSHA256(String data, String key) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKey);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error calculating HMAC SHA256", e);
        }
    }
}
