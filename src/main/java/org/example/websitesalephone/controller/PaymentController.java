package org.example.websitesalephone.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public CommonResponse createPayment(@RequestParam String orderId, HttpServletRequest request) {
        log.info("Request to create PayOS Payment Link for orderId: {}", orderId);
        return paymentService.createPaymentUrl(orderId, request);
    }

    @GetMapping("/payos-return")
    public void payosReturn(@RequestParam Map<String, String> queryParams, HttpServletResponse response) throws IOException {
        log.info("Received PayOS Return URL with params: {}", queryParams);
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
            frontendUrl = "http://localhost:5173/customer/payment-success?status=PAID"
                    + "&orderCode=" + queryParams.get("orderCode")
                    + "&paymentLinkId=" + queryParams.get("id")
                    + "&orderId=" + orderId;
        } else {
            frontendUrl = "http://localhost:5173/customer/payment-failed?status=CANCELLED"
                    + "&orderCode=" + queryParams.get("orderCode")
                    + "&orderId=" + orderId;
        }

        response.sendRedirect(frontendUrl);
    }

    @PostMapping("/payos-webhook")
    public CommonResponse payosWebhook(@RequestBody String rawBody) {
        log.info("Received PayOS Webhook notification payload");
        try {
            return paymentService.handleWebhook(rawBody);
        } catch (Exception e) {
            log.error("Error processing PayOS Webhook: ", e);
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_INTERNAL_ERROR)
                    .message("Failed to process webhook: " + e.getMessage())
                    .build();
        }
    }
}
