package org.example.websitesalephone.service.order.impl;

import lombok.RequiredArgsConstructor;
import org.example.websitesalephone.auth.UserDetail;
import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.comon.PageResponse;
import org.example.websitesalephone.dto.order.*;
import org.example.websitesalephone.entity.Order;
import org.example.websitesalephone.entity.OrderStatusHistory;
import org.example.websitesalephone.entity.User;
import org.example.websitesalephone.enums.OrderStatus;
import org.example.websitesalephone.enums.RoleEnums;
import org.example.websitesalephone.repository.OrderRepository;
import org.example.websitesalephone.repository.OrderStatusHistoryRepository;
import org.example.websitesalephone.repository.ProductRepository;
import org.example.websitesalephone.repository.UserRepository;
import org.example.websitesalephone.service.order.OrderService;
import org.example.websitesalephone.spe.OrderSpecification;
import org.example.websitesalephone.utils.Utils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    private final UserRepository userRepository;

    private final ProductRepository productRepository;

    @Override
    public CommonResponse search(OrderSearch searchForm) {

        PageRequest pageRequest = Utils.getPaging(searchForm);

        Specification<Order> spec = OrderSpecification.search(searchForm);
        User loginUser = getCurrentUser();
        String sellerId = null;

        if (isStaff(loginUser)) {
            sellerId = loginUser.getId();
            spec = spec.and(sellerOrderScope(sellerId));
        }

        final String scopedSellerId = sellerId;
        Page<OrderResponse> result = orderRepository
                .findAll(spec, pageRequest)
                .map(order -> scopedSellerId == null ? OrderResponse.fromOrder(order) : OrderResponse.fromSellerOrder(order, scopedSellerId));

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(PageResponse.from(result))
                .build();
    }

    @Override
    public CommonResponse detail(String id) {
        Order order = orderRepository.findById(id).orElse(null);

        if (order == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_SUCCESS)
                    .message("Order not found")
                    .build();
        }
        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(OrderDetailResponse.fromEntity(order))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResponse update(OrderRequest orderRequest) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetail userDetail = (UserDetail) auth.getPrincipal();

        User loginUser = userRepository.findByUsernameAndIsDeleted(userDetail.getLoginId(), false)
                .orElse(null);

        if (loginUser == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("User not found")
                    .build();
        }

        Order order = orderRepository.findById(orderRequest.getId()).orElse(null);

        if (order == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Order not found")
                    .build();
        }

        OrderStatus currentStatus = OrderStatus.fromCode(order.getStatus());

        OrderStatus newStatus;

        if (Objects.equals(orderRequest.getStatus(), OrderStatus.CANCELLED.getCode())) {
            newStatus = OrderStatus.CANCELLED;

        } else {
            int nextStep = currentStatus.getStep() + 1;
            newStatus = OrderStatus.fromStep(nextStep);
        }

        order.setStatus(newStatus.getCode());

        if (newStatus == OrderStatus.CONFIRMED && orderRequest.getShippingFee() != null) {
            order.setShippingFee(orderRequest.getShippingFee());
        }

        if (newStatus == OrderStatus.COMPLETED) {
            order.setDateTimeCheckout(OffsetDateTime.now());
        }

        order.setStaff(loginUser);
        orderRepository.saveAndFlush(order);

        // Lưu mô tả
        OrderStatusHistory orderStatusHistory = new OrderStatusHistory();
        orderStatusHistory.setId(UUID.randomUUID().toString());
        orderStatusHistory.setOrder(order);
        orderStatusHistory.setDescription(orderRequest.getDescription());
        orderStatusHistory.setStatus(order.getStatus());
        orderStatusHistoryRepository.saveAndFlush(orderStatusHistory);

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .message("Cập nhật trạng thái đơn hàng thành công")
                .data(OrderResponse.fromOrder(order))
                .build();
    }

    @Override
    public CommonResponse getListHistory(String id) {
        List<OrderStatusHistory> statusHistories = orderStatusHistoryRepository.findByOrder_id(id);

        if (statusHistories.isEmpty()) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .data(new ArrayList<>())
                    .build();
        }

        List<OrderStatusHistoryResponse> orderStatusHistoryResponses =
                statusHistories.stream().map(OrderStatusHistoryResponse::from).toList();

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(orderStatusHistoryResponses)
                .build();
    }

    @Override
    public CommonResponse getListOrderByUser(OrderByUserRequest orderByUserRequest) {
        Specification<Order> spec = OrderSpecification.search(orderByUserRequest);

        List<OrderResponse> orderList = orderRepository.findAll(spec).stream().map(OrderResponse::fromOrder).toList();
        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(orderList.isEmpty() ? new ArrayList<>() : orderList)
                .build();
    }

    @Override
    public CommonResponse countOrderByUser(CountOrderRequest countOrderRequest) {
        int countByStatusAndCustomerId = orderRepository.countByStatusAndCustomer_Id(countOrderRequest.getStatus(), countOrderRequest.getUserId());
        int countByCustomerId = orderRepository.countByCustomer_Id(countOrderRequest.getUserId());
        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(Objects.equals(countOrderRequest.getStatus(), "ALL") ? countByCustomerId : countByStatusAndCustomerId)
                .build();
    }

    @Override
    public CommonResponse countOrderByStaff(CountOrderRequest req) {
        User loginUser = getCurrentUser();

        if (loginUser == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("User not found")
                    .build();
        }

        boolean isAdmin = loginUser.getRole().getRoleEnums() == RoleEnums.ADMIN;
        String status = req.getStatus() == null ? "ALL" : req.getStatus();
        boolean isAll = "ALL".equals(status);

        if (isAdmin) {
            int total = orderRepository.countAllByIsDeletedFalse();
            int byStatus = orderRepository.countByStatus(status);

            return CommonResponse.builder()
                    .code(CommonResponse.CODE_SUCCESS)
                    .data(isAll ? total : byStatus)
                    .build();
        }

        int totalBySeller = orderRepository.countBySellerIdAndStatus(loginUser.getId(), "ALL");
        int byStatusAndSeller = orderRepository.countBySellerIdAndStatus(loginUser.getId(), status);

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(isAll ? totalBySeller : byStatusAndSeller)
                .build();
    }

    @Override
    public CommonResponse countDashBoard(String searchText) {
        User loginUser = getCurrentUser();
        Object result;

        if (isStaff(loginUser)) {
            switch (searchText) {
                case "PRODUCT" -> result = productRepository.countSellableProductsBySellerId(loginUser.getId());
                case "ORDER" -> result = orderRepository.countBySellerIdAndStatus(loginUser.getId(), "ALL");
                case "CUSTOMER" -> result = 0;
                case "CANCELLED" -> result = orderRepository.countBySellerIdAndStatus(loginUser.getId(), "CANCELLED");
                case "REVENUE" -> result = orderRepository.getRevenueBySellerId(loginUser.getId());
                default -> {
                    return dashboardMetricNotFound();
                }
            }
        } else {
            switch (searchText) {
                case "PRODUCT" -> result = productRepository.countByVariantsIsNotEmpty();
                case "ORDER" -> result = orderRepository.countAllByIsDeletedFalse();
                case "CUSTOMER" -> result = userRepository.countByIsDeletedFalse();
                case "CANCELLED" -> result = orderRepository.countByStatus("CANCELLED");
                case "REVENUE" -> result = orderRepository.getRevenueByStatus();
                default -> {
                    return dashboardMetricNotFound();
                }
            }
        }

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(result)
                .build();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetail userDetail)) {
            return null;
        }

        return userRepository.findByUsernameAndIsDeleted(userDetail.getLoginId(), false).orElse(null);
    }

    private boolean isStaff(User user) {
        return user != null
                && user.getRole() != null
                && user.getRole().getRoleEnums() == RoleEnums.STAFF;
    }

    private Specification<Order> sellerOrderScope(String sellerId) {
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            return cb.equal(
                    root.join("orderItems")
                            .join("productVariant")
                            .join("product")
                            .join("shopRegistration")
                            .join("user")
                            .get("id"),
                    sellerId
            );
        };
    }

    private CommonResponse dashboardMetricNotFound() {
        return CommonResponse.builder()
                .code(CommonResponse.CODE_NOT_FOUND)
                .message("Loại thống kê không tồn tại")
                .build();
    }
}
