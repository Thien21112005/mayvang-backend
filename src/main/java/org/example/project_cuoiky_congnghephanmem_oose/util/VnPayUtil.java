package org.example.project_cuoiky_congnghephanmem_oose.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class VnPayUtil {

    @Value("${vnpay.tmnCode}")
    private String tmnCode;

    @Value("${vnpay.hashSecret}")
    private String hashSecret;

    @Value("${vnpay.payUrl}")
    private String payUrl;

    @Value("${vnpay.returnUrl}")
    private String returnUrl;

    public String createPaymentUrl(int bookingId, long amount, String ipAddress) {
        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100));
        vnpParams.put("vnp_CurrCode", "VND");
        // TxnRef phải UNIQUE mỗi lần gọi VNPAY (VNPAY từ chối TxnRef trùng trong 24h)
        String txnRef = bookingId + "_" + System.currentTimeMillis();
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", "Thanh toan booking #" + bookingId);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", ipAddress);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnpParams.put("vnp_CreateDate", formatter.format(calendar.getTime()));
        calendar.add(Calendar.MINUTE, 15);
        vnpParams.put("vnp_ExpireDate", formatter.format(calendar.getTime()));

        String queryUrl = buildQuery(vnpParams, false);
        String hashData = buildQuery(vnpParams, true);
        String secureHash = hmacSHA512(hashSecret, hashData);

        return payUrl + "?" + queryUrl + "&vnp_SecureHash=" + secureHash;
    }

    public boolean validateReturn(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        Map<String, String> cloned = new TreeMap<>(params);
        cloned.remove("vnp_SecureHash");
        cloned.remove("vnp_SecureHashType");

        String signData = buildQuery(cloned, true);
        String calculatedHash = hmacSHA512(hashSecret, signData);
        return calculatedHash.equals(receivedHash);
    }

    private String buildQuery(Map<String, String> params, boolean encodeValue) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) continue;
            if (!sb.isEmpty()) sb.append("&");
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII));
            sb.append("=");
            sb.append(encodeValue
                    ? URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII)
                    : URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
        }
        return sb.toString();
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi ký VNPay", ex);
        }
    }
}