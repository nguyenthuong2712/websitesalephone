package org.example.websitesalephone.controller;

import lombok.RequiredArgsConstructor;
import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.service.payment.PaymentService;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public CommonResponse createPayment(@RequestParam String orderId, HttpServletRequest request) {
        return paymentService.createPaymentUrl(orderId, request);
    }

    @GetMapping("/vnpay-return")
    public void vnpayReturn(@RequestParam Map<String, String> queryParams, HttpServletResponse response) throws IOException {
        CommonResponse result = paymentService.handleCallback(queryParams);

        boolean isSuccess = false;
        String orderId = "";

        if (result.getCode() == CommonResponse.CODE_SUCCESS && result.getData() instanceof Map) {
            Map<?, ?> data = (Map<?, ?>) result.getData();
            isSuccess = Boolean.TRUE.equals(data.get("isSuccess"));
            orderId = String.valueOf(data.get("orderId"));
        }

        String frontendUrl;
        if (isSuccess) {
            frontendUrl = "http://localhost:5173/customer/payment-success?vnp_ResponseCode=" + queryParams.get("vnp_ResponseCode")
                    + "&vnp_TransactionNo=" + queryParams.get("vnp_TransactionNo")
                    + "&vnp_Amount=" + queryParams.get("vnp_Amount")
                    + "&vnp_OrderInfo=" + URLEncoderForFrontend(queryParams.get("vnp_OrderInfo"))
                    + "&orderId=" + orderId;
        } else {
            frontendUrl = "http://localhost:5173/customer/payment-failed?vnp_ResponseCode=" + queryParams.get("vnp_ResponseCode")
                    + "&orderId=" + orderId;
        }

        response.sendRedirect(frontendUrl);
    }

    private String URLEncoderForFrontend(String value) {
        if (value == null) return "";
        try {
            return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return value;
        }
    }
}
