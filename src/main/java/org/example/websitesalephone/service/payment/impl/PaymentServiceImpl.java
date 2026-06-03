package org.example.websitesalephone.service.payment.impl;

import lombok.RequiredArgsConstructor;
import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.config.VnPayConfig;
import org.example.websitesalephone.entity.Order;
import org.example.websitesalephone.entity.Payment;
import org.example.websitesalephone.entity.OrderStatusHistory;
import org.example.websitesalephone.entity.User;
import org.example.websitesalephone.enums.OrderStatus;
import org.example.websitesalephone.repository.OrderRepository;
import org.example.websitesalephone.repository.PaymentRepository;
import org.example.websitesalephone.repository.OrderStatusHistoryRepository;
import org.example.websitesalephone.repository.UserRepository;
import org.example.websitesalephone.service.payment.PaymentService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.example.websitesalephone.auth.UserDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final UserRepository userRepository;
    private final VnPayConfig vnPayConfig;

    @Override
    @Transactional(readOnly = true)
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

        BigDecimal amount = order.getTotalAmount().multiply(new BigDecimal("100"));
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT-7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(amount.longValue()));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", order.getId());
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderCode());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", VnPayConfig.getIpAddress(request));
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        try {
            for (int i = 0; i < fieldNames.size(); i++) {
                String fieldName = fieldNames.get(i);
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    String encodedName = URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString());
                    String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()).replace("+", "%20");
                    hashData.append(fieldName).append("=").append(encodedValue);
                    query.append(encodedName).append("=").append(encodedValue);
                    if (i < fieldNames.size() - 1) {
                        query.append("&");
                        hashData.append("&");
                    }
                }
            }
        } catch (Exception e) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_INTERNAL_ERROR)
                    .message("Lỗi tạo url thanh toán: " + e.getMessage())
                    .build();
        }

        String vnp_SecureHash = VnPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        String paymentUrl = vnPayConfig.getPayUrl() + "?" + query.toString() + "&vnp_SecureHash=" + vnp_SecureHash;

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(paymentUrl)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResponse handleCallback(Map<String, String> queryParams) {
        String vnp_SecureHash = queryParams.get("vnp_SecureHash");
        if (vnp_SecureHash == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Thiếu secure hash")
                    .build();
        }

        Map<String, String> fields = new TreeMap<>();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (entry.getKey().startsWith("vnp_") && !entry.getKey().equals("vnp_SecureHash") && !entry.getKey().equals("vnp_SecureHashType")) {
                fields.put(entry.getKey(), entry.getValue());
            }
        }

        StringBuilder signData = new StringBuilder();
        Iterator<Map.Entry<String, String>> itr = fields.entrySet().iterator();
        try {
            while (itr.hasNext()) {
                Map.Entry<String, String> entry = itr.next();
                signData.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII.toString()).replace("+", "%20"));
                if (itr.hasNext()) {
                    signData.append("&");
                }
            }
        } catch (Exception e) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_INTERNAL_ERROR)
                    .message("Lỗi mã hóa dữ liệu chữ ký")
                    .build();
        }

        String checkSum = VnPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), signData.toString());
        if (!checkSum.equalsIgnoreCase(vnp_SecureHash)) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Chữ ký bảo mật không trùng khớp")
                    .build();
        }

        String orderId = queryParams.get("vnp_TxnRef");
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Không tìm thấy đơn hàng")
                    .build();
        }

        String responseCode = queryParams.get("vnp_ResponseCode");
        boolean isSuccess = "00".equals(responseCode);

        Payment payment = new Payment();
        payment.setId(UUID.randomUUID().toString());
        payment.setOrder(order);
        payment.setMethod("VNPAY");
        payment.setAmount(order.getTotalAmount());
        payment.setPaidAt(LocalDateTime.now());
        payment.setTransactionNo(queryParams.get("vnp_TransactionNo"));

        if (isSuccess) {
            payment.setStatus("SUCCESS");
            order.setStatus(OrderStatus.CONFIRMED.getCode());
            order.setMethodTransaction("VNPAY");
            order.setStatusTransaction("PAID");
            orderRepository.saveAndFlush(order);

            OrderStatusHistory history = new OrderStatusHistory();
            history.setId(UUID.randomUUID().toString());
            history.setOrder(order);
            history.setStatus(OrderStatus.CONFIRMED.getCode());
            history.setDescription("Đã thanh toán thành công qua VNPAY. Mã GD: " + queryParams.get("vnp_TransactionNo"));
            orderStatusHistoryRepository.saveAndFlush(history);
        } else {
            payment.setStatus("FAILED");
            order.setStatusTransaction("FAILED");
            orderRepository.saveAndFlush(order);

            OrderStatusHistory history = new OrderStatusHistory();
            history.setId(UUID.randomUUID().toString());
            history.setOrder(order);
            history.setStatus(order.getStatus());
            history.setDescription("Giao dịch thanh toán VNPAY thất bại. Mã lỗi: " + responseCode);
            orderStatusHistoryRepository.saveAndFlush(history);
        }

        paymentRepository.saveAndFlush(payment);

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(Map.of("isSuccess", isSuccess, "orderId", orderId))
                .build();
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetail userDetail)) {
            return null;
        }
        return userRepository.findByUsernameAndIsDeleted(userDetail.getLoginId(), false).orElse(null);
    }
}
