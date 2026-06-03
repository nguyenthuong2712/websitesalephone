package org.example.websitesalephone.service.order.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.example.websitesalephone.auth.UserDetail;
import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.comon.PageResponse;
import org.example.websitesalephone.dto.order.*;
import org.example.websitesalephone.entity.Order;
import org.example.websitesalephone.entity.OrderStatusHistory;
import org.example.websitesalephone.entity.Product;
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
    @Transactional(readOnly = true)
    public CommonResponse search(OrderSearch searchForm) {

        PageRequest pageRequest = Utils.getPaging(searchForm);

        Specification<Order> spec = OrderSpecification.search(searchForm);

        Page<OrderResponse> result = orderRepository
                .findAll(spec, pageRequest)
                .map(OrderResponse::fromOrder);

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(PageResponse.from(result))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CommonResponse detail(String id) {
        User loginUser = getAuthenticatedUser();
        if (loginUser == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_ACCOUNT_EXCEPTION)
                    .message("Vui lòng đăng nhập")
                    .build();
        }

        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Order not found")
                    .build();
        }

        if (isCustomer(loginUser) && !Objects.equals(order.getCustomer().getId(), loginUser.getId())) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_ACCOUNT_EXCEPTION)
                    .message("Bạn không có quyền xem đơn hàng này")
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
        User loginUser = getAuthenticatedUser();
        if (loginUser == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_ACCOUNT_EXCEPTION)
                    .message("Vui lòng đăng nhập")
                    .build();
        }

        Order order = orderRepository.findById(orderRequest.getId()).orElse(null);

        if (order == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Order not found")
                    .build();
        }

        if (isCustomer(loginUser)) {
            if (!Objects.equals(order.getCustomer().getId(), loginUser.getId())) {
                return CommonResponse.builder()
                        .code(CommonResponse.CODE_ACCOUNT_EXCEPTION)
                        .message("Bạn không có quyền cập nhật đơn hàng này")
                        .build();
            }
            if (!Objects.equals(orderRequest.getStatus(), OrderStatus.CANCELLED.getCode())) {
                return CommonResponse.builder()
                        .code(CommonResponse.CODE_BUSINESS)
                        .message("Khách hàng chỉ được phép hủy đơn hàng")
                        .build();
            }
            OrderStatus currentStatus = OrderStatus.fromCode(order.getStatus());
            if (currentStatus != OrderStatus.PENDING && currentStatus != OrderStatus.CONFIRMED) {
                return CommonResponse.builder()
                        .code(CommonResponse.CODE_BUSINESS)
                        .message("Đơn hàng chỉ có thể hủy khi ở trạng thái chờ xử lý hoặc đã xác nhận")
                        .build();
            }
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

        if (!isCustomer(loginUser)) {
            order.setStaff(loginUser);
        }
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
    @Transactional(readOnly = true)
    public CommonResponse getListHistory(String id) {
        User loginUser = getAuthenticatedUser();
        if (loginUser == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_ACCOUNT_EXCEPTION)
                    .message("Vui lòng đăng nhập")
                    .build();
        }

        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .data(new ArrayList<>())
                    .message("Order not found")
                    .build();
        }

        if (isCustomer(loginUser) && !Objects.equals(order.getCustomer().getId(), loginUser.getId())) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_ACCOUNT_EXCEPTION)
                    .message("Bạn không có quyền xem lịch sử đơn hàng này")
                    .build();
        }

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
    @Transactional(readOnly = true)
    public CommonResponse getListOrderByUser(OrderByUserRequest orderByUserRequest) {
        User loginUser = getAuthenticatedUser();
        if (loginUser == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_ACCOUNT_EXCEPTION)
                    .message("Vui lòng đăng nhập")
                    .build();
        }

        if (isCustomer(loginUser) && StringUtils.isNotBlank(orderByUserRequest.getId())
                && !Objects.equals(orderByUserRequest.getId(), loginUser.getId())) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_ACCOUNT_EXCEPTION)
                    .message("Bạn không có quyền xem danh sách đơn hàng này")
                    .build();
        }

        Specification<Order> spec = OrderSpecification.search(orderByUserRequest);
        List<OrderResponse> orderList = orderRepository.findAll(spec).stream()
                .filter(order -> !isCustomer(loginUser) || Objects.equals(order.getCustomer().getId(), loginUser.getId()))
                .map(OrderResponse::fromOrder)
                .toList();

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(orderList.isEmpty() ? new ArrayList<>() : orderList)
                .build();
    }

    @Override
    public CommonResponse countOrderByUser(CountOrderRequest countOrderRequest) {
        User loginUser = getAuthenticatedUser();
        if (loginUser == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_ACCOUNT_EXCEPTION)
                    .message("Vui lòng đăng nhập")
                    .build();
        }

        String userId = countOrderRequest.getUserId();
        if (isCustomer(loginUser)) {
            if (StringUtils.isNotBlank(userId) && !Objects.equals(userId, loginUser.getId())) {
                return CommonResponse.builder()
                        .code(CommonResponse.CODE_ACCOUNT_EXCEPTION)
                        .message("Bạn không có quyền xem thống kê của người dùng khác")
                        .build();
            }
            userId = loginUser.getId();
        }

        if (StringUtils.isBlank(userId)) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Thiếu thông tin người dùng")
                    .build();
        }

        int countByStatusAndCustomerId = orderRepository.countByStatusAndCustomer_Id(countOrderRequest.getStatus(), userId);
        int countByCustomerId = orderRepository.countByCustomer_Id(userId);
        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(Objects.equals(countOrderRequest.getStatus(), "ALL") ? countByCustomerId : countByStatusAndCustomerId)
                .build();
    }

    @Override
    public CommonResponse countOrderByStaff(CountOrderRequest req) {
        User loginUser = getAuthenticatedUser();
        if (loginUser == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_ACCOUNT_EXCEPTION)
                    .message("Vui lòng đăng nhập")
                    .build();
        }

        if (loginUser.getRole() == null || (loginUser.getRole().getRoleEnums() != RoleEnums.ADMIN
                && loginUser.getRole().getRoleEnums() != RoleEnums.STAFF)) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_ACCOUNT_EXCEPTION)
                    .message("Bạn không có quyền xem thống kê này")
                    .build();
        }

        if (StringUtils.isBlank(req.getStatus())) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Thiếu trạng thái đơn hàng")
                    .build();
        }



        boolean isAdmin = loginUser.getRole().getRoleEnums() == RoleEnums.ADMIN;
        boolean isAll = "ALL".equals(req.getStatus());

        if (isAdmin) {
            int total = orderRepository.countAllByIsDeletedFalse();
            int byStatus = orderRepository.countByStatus(req.getStatus());

            return CommonResponse.builder()
                    .code(CommonResponse.CODE_SUCCESS)
                    .data(isAll ? total : byStatus)
                    .build();
        }

        // Staff
        int totalByStaff = orderRepository.countByStaff_Id(loginUser.getId());
        int byStatusAndStaff = orderRepository.countByStatusAndStaff_Id(req.getStatus(), loginUser.getId());

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(isAll ? totalByStaff : byStatusAndStaff)
                .build();
    }

    @Override
    public CommonResponse countDashBoard(String searchText) {
        User loginUser = getAuthenticatedUser();
        if (loginUser == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_ACCOUNT_EXCEPTION)
                    .message("Vui lòng đăng nhập")
                    .build();
        }

        if (loginUser.getRole() == null || (loginUser.getRole().getRoleEnums() != RoleEnums.ADMIN
                && loginUser.getRole().getRoleEnums() != RoleEnums.STAFF)) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_ACCOUNT_EXCEPTION)
                    .message("Bạn không có quyền xem thống kê này")
                    .build();
        }

        Object result;

        switch (searchText) {
            case "PRODUCT" -> {
                result = productRepository.countByIsDeletedFalse();
            }
            case "ORDER" -> {
                result = orderRepository.countAllByIsDeletedFalse();
            }
            case "CUSTOMER" -> {
                result = userRepository.countByIsDeletedFalse();
            }
            case "CANCELLED" -> {
                result = orderRepository.countByStatus("CANCELLED");
            }
            case "REVENUE" -> {
                result = orderRepository.getRevenueByStatus();
            }
            default -> {
                return CommonResponse.builder()
                        .code(CommonResponse.CODE_NOT_FOUND)
                        .message("Loại thống kê không tồn tại")
                        .build();
            }
        }

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(result)
                .build();
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetail userDetail)) {
            return null;
        }
        return userRepository.findByUsernameAndIsDeleted(userDetail.getLoginId(), false).orElse(null);
    }

    private boolean isCustomer(User user) {
        return user.getRole() != null && user.getRole().getRoleEnums() == RoleEnums.CUSTOMER;
    }
}
