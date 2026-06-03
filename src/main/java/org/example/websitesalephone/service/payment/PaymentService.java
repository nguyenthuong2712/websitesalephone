package org.example.websitesalephone.service.payment;

import org.example.websitesalephone.comon.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface PaymentService {
    CommonResponse createPaymentUrl(String orderId, HttpServletRequest request);
    CommonResponse handleCallback(Map<String, String> queryParams);
}
