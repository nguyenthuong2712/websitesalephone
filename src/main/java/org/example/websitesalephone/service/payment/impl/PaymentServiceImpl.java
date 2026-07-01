package org.example.websitesalephone.service.payment.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.config.PayOSConfig;
import org.example.websitesalephone.entity.Order;
import org.example.websitesalephone.entity.Payment;
import org.example.websitesalephone.entity.OrderStatusHistory;
import org.example.websitesalephone.entity.User;
import org.example.websitesalephone.enums.OrderStatus;
import org.example.websitesalephone.repository.OrderRepository;
import org.example.websitesalephone.repository.PaymentRepository;
import org.example.websitesalephone.repository.OrderStatusHistoryRepository;
import org.example.websitesalephone.repository.UserRepository;
import org.example.websitesalephone.entity.OrderItem;
import org.example.websitesalephone.entity.ProductVariant;
import org.example.websitesalephone.repository.ProductVariantRepository;
import org.example.websitesalephone.service.payment.PaymentService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.example.websitesalephone.auth.UserDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final UserRepository userRepository;
    private final PayOSConfig payOSConfig;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public CommonResponse createPaymentUrl(String orderId, HttpServletRequest request) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_ACCOUNT_EXCEPTION)
                    .message("Vui lòng đăng nhập")
                    .build();
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Không tìm thấy đơn hàng")
                    .build();
        }

        if (!Objects.equals(order.getCustomer().getId(), user.getId())) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_ACCOUNT_EXCEPTION)
                    .message("Bạn không có quyền thanh toán đơn hàng của người khác")
                    .build();
        }

        if (OrderStatus.COMPLETED.getCode().equals(order.getStatus()) || OrderStatus.CANCELLED.getCode().equals(order.getStatus())) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Trạng thái đơn hàng không hợp lệ để thanh toán")
                    .build();
        }

        if (order.getPayosOrderCode() == null) {
            long generatedCode = generatePayOSOrderCode();
            while (orderRepository.findByPayosOrderCode(generatedCode).isPresent()) {
                generatedCode = generatePayOSOrderCode();
            }
            order.setPayosOrderCode(generatedCode);
            orderRepository.saveAndFlush(order);
        }

        int totalAmount = order.getTotalAmount().intValue();
        String description = "DH " + order.getOrderCode();
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }

        long orderCode = order.getPayosOrderCode();
        String returnUrl = payOSConfig.getReturnUrl() + "?orderId=" + order.getId();
        String cancelUrl = payOSConfig.getCancelUrl() + "?orderId=" + order.getId();

        // Create PayOS signature: amount={amount}&cancelUrl={cancelUrl}&description={description}&orderCode={orderCode}&returnUrl={returnUrl}
        String signatureData = String.format("amount=%d&cancelUrl=%s&description=%s&orderCode=%d&returnUrl=%s",
                totalAmount, cancelUrl, description, orderCode, returnUrl);
        String signature = PayOSConfig.hmacSHA256(signatureData, payOSConfig.getChecksumKey());

        Map<String, Object> body = new HashMap<>();
        body.put("orderCode", orderCode);
        body.put("amount", totalAmount);
        body.put("description", description);
        body.put("returnUrl", returnUrl);
        body.put("cancelUrl", cancelUrl);
        body.put("signature", signature);

        if (order.getOrderItems() != null) {
            List<Map<String, Object>> items = new ArrayList<>();
            for (var item : order.getOrderItems()) {
                String itemName = item.getProductVariant().getProduct().getName();
                if (itemName.length() > 50) {
                    itemName = itemName.substring(0, 50);
                }
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("name", itemName);
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("price", item.getUnitPrice().intValue());
                items.add(itemMap);
            }
            body.put("items", items);
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-client-id", payOSConfig.getClientId());
            headers.set("x-api-key", payOSConfig.getApiKey());
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            String url = "https://api-merchant.payos.vn/v2/payment-requests";
            log.info("Sending payment creation request to PayOS endpoint for orderCode: {}", orderCode);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse = mapper.readTree(response.getBody());

            if (jsonResponse.has("code") && "00".equals(jsonResponse.get("code").asText())) {
                String checkoutUrl = jsonResponse.get("data").get("checkoutUrl").asText();
                return CommonResponse.builder()
                        .code(CommonResponse.CODE_SUCCESS)
                        .data(checkoutUrl)
                        .build();
            } else {
                String desc = jsonResponse.has("desc") ? jsonResponse.get("desc").asText() : "Lỗi không xác định";
                log.error("PayOS response error: {}", desc);
                return CommonResponse.builder()
                        .code(CommonResponse.CODE_BUSINESS)
                        .message("Lỗi tạo link thanh toán PayOS: " + desc)
                        .build();
            }
        } catch (Exception e) {
            log.error("Exception in createPaymentUrl: ", e);
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_INTERNAL_ERROR)
                    .message("Lỗi tạo url thanh toán PayOS: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResponse handleCallback(Map<String, String> queryParams) {
        log.info("Handling PayOS Return Callback with params: {}", queryParams);
        String orderId = queryParams.get("orderId");
        if (orderId == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Thiếu orderId trong callback query")
                    .build();
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Không tìm thấy đơn hàng")
                    .build();
        }

        String status = queryParams.get("status");
        boolean isSuccess = "PAID".equalsIgnoreCase(status);

        Payment existingPayment = paymentRepository.findAll().stream()
                .filter(p -> p.getOrder() != null && order.getId().equals(p.getOrder().getId()))
                .findFirst()
                .orElse(null);

        if (existingPayment != null && "SUCCESS".equals(existingPayment.getStatus())) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_SUCCESS)
                    .data(Map.of("isSuccess", true, "orderId", orderId))
                    .build();
        }

        Payment payment = existingPayment != null ? existingPayment : new Payment();
        if (payment.getId() == null) {
            payment.setId(UUID.randomUUID().toString());
        }
        payment.setOrder(order);
        payment.setMethod("PAYOS");
        payment.setAmount(order.getTotalAmount());
        payment.setPaidAt(LocalDateTime.now());
        payment.setTransactionNo(queryParams.get("paymentLinkId"));

        if (isSuccess) {
            payment.setStatus("SUCCESS");
            order.setStatus(OrderStatus.CONFIRMED.getCode());
            order.setMethodTransaction("PAYOS");
            order.setStatusTransaction("PAID");
            orderRepository.saveAndFlush(order);

            OrderStatusHistory history = new OrderStatusHistory();
            history.setId(UUID.randomUUID().toString());
            history.setOrder(order);
            history.setStatus(OrderStatus.CONFIRMED.getCode());
            history.setDescription("Đã thanh toán thành công qua PayOS. Mã GD: " + queryParams.get("paymentLinkId"));
            orderStatusHistoryRepository.saveAndFlush(history);
        } else {
            payment.setStatus("CANCELLED");
            order.setStatusTransaction("UNPAID");
            order.setStatus(OrderStatus.CANCELLED.getCode());

            // Hoàn trả số lượng tồn kho sản phẩm khi hủy thanh toán
            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    ProductVariant variant = item.getProductVariant();
                    if (variant != null) {
                        variant.setQuantity(variant.getQuantity() + item.getQuantity());
                        productVariantRepository.saveAndFlush(variant);
                    }
                }
            }
            orderRepository.saveAndFlush(order);

            OrderStatusHistory history = new OrderStatusHistory();
            history.setId(UUID.randomUUID().toString());
            history.setOrder(order);
            history.setStatus(OrderStatus.CANCELLED.getCode());
            history.setDescription("Giao dịch thanh toán PayOS bị hủy hoặc thất bại. Đơn hàng đã tự động hủy. Trạng thái: " + status);
            orderStatusHistoryRepository.saveAndFlush(history);
        }

        paymentRepository.saveAndFlush(payment);

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(Map.of("isSuccess", isSuccess, "orderId", orderId))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResponse handleWebhook(String webhookBody) {
        log.info("Processing PayOS Webhook payload verification");
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(webhookBody);

            if (!rootNode.has("data") || !rootNode.has("signature")) {
                return CommonResponse.builder()
                        .code(CommonResponse.CODE_BUSINESS)
                        .message("Invalid webhook format")
                        .build();
            }

            JsonNode dataNode = rootNode.get("data");
            String receivedSignature = rootNode.get("signature").asText();

            // Verify signature by sorting data keys alphabetically
            Map<String, String> dataMap = new TreeMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = dataNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String value = field.getValue().isNull() ? "" : field.getValue().asText();
                dataMap.put(field.getKey(), value);
            }

            StringBuilder signStr = new StringBuilder();
            int i = 0;
            for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                signStr.append(entry.getKey()).append("=").append(entry.getValue());
                if (i < dataMap.size() - 1) {
                    signStr.append("&");
                }
                i++;
            }

            String computedSignature = PayOSConfig.hmacSHA256(signStr.toString(), payOSConfig.getChecksumKey());
            if (!computedSignature.equalsIgnoreCase(receivedSignature)) {
                log.warn("PayOS webhook checksum mismatch");
                return CommonResponse.builder()
                        .code(CommonResponse.CODE_BUSINESS)
                        .message("Signature mismatch")
                        .build();
            }

            long orderCode = dataNode.get("orderCode").asLong();
            log.info("PayOS Webhook verified successfully for orderCode: {}", orderCode);

            Order order = orderRepository.findByPayosOrderCode(orderCode).orElse(null);
            if (order == null) {
                log.warn("Order not found for PayOS orderCode: {}", orderCode);
                return CommonResponse.builder()
                        .code(CommonResponse.CODE_NOT_FOUND)
                        .message("Order not found")
                        .build();
            }

            String code = rootNode.has("code") ? rootNode.get("code").asText() : "";
            boolean isSuccess = "00".equals(code);

            String transactionRef = dataNode.has("reference") ? dataNode.get("reference").asText() : null;
            Payment existingPayment = paymentRepository.findAll().stream()
                    .filter(p -> p.getOrder() != null && order.getId().equals(p.getOrder().getId()))
                    .findFirst()
                    .orElse(null);

            if (existingPayment != null && "SUCCESS".equals(existingPayment.getStatus())) {
                log.info("Webhook duplicate check: Order {} already paid", order.getId());
                return CommonResponse.builder()
                        .code(CommonResponse.CODE_SUCCESS)
                        .message("Webhook already processed successfully")
                        .build();
            }

            Payment payment = existingPayment != null ? existingPayment : new Payment();
            if (payment.getId() == null) {
                payment.setId(UUID.randomUUID().toString());
            }
            payment.setOrder(order);
            payment.setMethod("PAYOS");
            payment.setAmount(BigDecimal.valueOf(dataNode.has("amount") ? dataNode.get("amount").asDouble() : 0));
            payment.setPaidAt(LocalDateTime.now());
            payment.setTransactionNo(transactionRef != null ? transactionRef : (dataNode.has("paymentLinkId") ? dataNode.get("paymentLinkId").asText() : ""));

            if (isSuccess) {
                payment.setStatus("SUCCESS");
                order.setStatus(OrderStatus.CONFIRMED.getCode());
                order.setMethodTransaction("PAYOS");
                order.setStatusTransaction("PAID");
                orderRepository.saveAndFlush(order);

                OrderStatusHistory history = new OrderStatusHistory();
                history.setId(UUID.randomUUID().toString());
                history.setOrder(order);
                history.setStatus(OrderStatus.CONFIRMED.getCode());
                history.setDescription("PayOS Webhook báo thanh toán thành công. Mã GD: " + transactionRef);
                orderStatusHistoryRepository.saveAndFlush(history);
            } else {
                payment.setStatus("FAILED");
                order.setStatusTransaction("UNPAID");
                order.setStatus(OrderStatus.CANCELLED.getCode());

                // Hoàn trả số lượng tồn kho sản phẩm khi hủy/thất bại thanh toán qua webhook
                if (order.getOrderItems() != null) {
                    for (OrderItem item : order.getOrderItems()) {
                        ProductVariant variant = item.getProductVariant();
                        if (variant != null) {
                            variant.setQuantity(variant.getQuantity() + item.getQuantity());
                            productVariantRepository.saveAndFlush(variant);
                        }
                    }
                }
                orderRepository.saveAndFlush(order);

                OrderStatusHistory history = new OrderStatusHistory();
                history.setId(UUID.randomUUID().toString());
                history.setOrder(order);
                history.setStatus(OrderStatus.CANCELLED.getCode());
                history.setDescription("PayOS Webhook báo giao dịch thất bại hoặc bị hủy. Mã lỗi: " + code);
                orderStatusHistoryRepository.saveAndFlush(history);
            }

            paymentRepository.saveAndFlush(payment);

            return CommonResponse.builder()
                    .code(CommonResponse.CODE_SUCCESS)
                    .message("Processed successfully")
                    .build();

        } catch (Exception e) {
            log.error("Exception during webhook signature verification/processing: ", e);
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Signature validation or processing error: " + e.getMessage())
                    .build();
        }
    }

    private long generatePayOSOrderCode() {
        String timePart = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("MMddHHmmss"));
        int randomPart = (int) (Math.random() * 9000) + 1000;
        return Long.parseLong(timePart + randomPart);
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetail userDetail)) {
            return null;
        }
        return userRepository.findByUsernameAndIsDeleted(userDetail.getLoginId(), false).orElse(null);
    }
}
