package com.vendingmachine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vendingmachine.repository.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class PayOSService {

    @Value("${payos.client-id:}")
    private String clientId;

    @Value("${payos.api-key:}")
    private String apiKey;

    @Value("${payos.checksum-key:}")
    private String checksumKey;

    @Value("${payos.return-url:}")
    private String returnUrl;

    @Value("${payos.cancel-url:}")
    private String cancelUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String createPaymentUrl(Transaction transaction) throws Exception {
        long orderCode = System.currentTimeMillis() % 1000000000L;
        transaction.setOrderCode(orderCode);

        int amount = (int) Math.round(transaction.calculateAmount());
        String description = transaction.getBeverage().getName();
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }

        String finalReturnUrl = returnUrl + "?orderCode=" + orderCode;
        String finalCancelUrl = cancelUrl + "?orderCode=" + orderCode;

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("orderCode", orderCode);
        paymentData.put("amount", amount);
        paymentData.put("description", description);
        paymentData.put("returnUrl", finalReturnUrl);
        paymentData.put("cancelUrl", finalCancelUrl);

        String signature = createSignature(paymentData);
        paymentData.put("signature", signature);

        return callPayOSAPI(paymentData);
    }

    public boolean isPaymentSuccess(String status, String code) {
        return "success".equals(status) || "00".equals(code);
    }

    public boolean isPaymentCancelled(String cancel, String status) {
        return "true".equals(cancel) || "CANCELLED".equals(status);
    }

    @SuppressWarnings("unchecked")
    private String callPayOSAPI(Map<String, Object> paymentData) throws Exception {
        String apiUrl = "https://api-merchant.payos.vn/v2/payment-requests";
        String jsonPayload = objectMapper.writeValueAsString(paymentData);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("x-client-id", clientId)
                .header("x-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Map<String, Object> responseData = objectMapper.readValue(response.body(), Map.class);
            if ("00".equals(responseData.get("code"))) {
                Map<String, Object> data = (Map<String, Object>) responseData.get("data");
                if (data != null && data.containsKey("checkoutUrl")) {
                    return (String) data.get("checkoutUrl");
                }
            }
            throw new RuntimeException("PayOS error: " + responseData.get("desc"));
        }
        throw new RuntimeException("PayOS HTTP error: " + response.statusCode());
    }

    private String createSignature(Map<String, Object> data) throws Exception {
        String raw = "amount=" + data.get("amount")
                + "&cancelUrl=" + data.get("cancelUrl")
                + "&description=" + data.get("description")
                + "&orderCode=" + data.get("orderCode")
                + "&returnUrl=" + data.get("returnUrl");

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(checksumKey.getBytes("UTF-8"), "HmacSHA256"));
        byte[] hash = mac.doFinal(raw.getBytes("UTF-8"));

        StringBuilder hex = new StringBuilder();
        for (byte b : hash) hex.append(String.format("%02x", b));
        return hex.toString();
    }
}
