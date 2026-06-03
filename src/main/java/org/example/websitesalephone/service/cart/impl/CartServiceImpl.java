package org.example.websitesalephone.service.cart.impl;

import lombok.RequiredArgsConstructor;
import org.example.websitesalephone.auth.UserDetail;
import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.dto.cart.CartRequest;
import org.example.websitesalephone.dto.cart.CartResponse;
import org.example.websitesalephone.dto.cart.CartSearch;
import org.example.websitesalephone.dto.cart.CheckOutRequest;
import org.example.websitesalephone.entity.*;
import org.example.websitesalephone.enums.CartStatus;
import org.example.websitesalephone.enums.OrderStatus;
import org.example.websitesalephone.repository.*;
import org.example.websitesalephone.service.cart.CartService;
import org.example.websitesalephone.utils.Utils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final ProductVariantRepository productVariantRepository;

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final UserRepository userRepository;

    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResponse addToCart(CartRequest request) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_ACCOUNT_EXCEPTION)
                    .message("Vui lòng đăng nhập")
                    .build();
        }

        if (request.getQuantity() <= 0) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Số lượng không hợp lệ")
                    .build();
        }

        ProductVariant product = productVariantRepository.findById(request.getProductId()).orElse(null);
        if (product == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Không tìm thấy sản phẩm")
                    .build();
        }

        if (request.getQuantity() > product.getQuantity()) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Số lượng sản phẩm không đủ trong kho")
                    .build();
        }

        Cart cart = cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setId(UUID.randomUUID().toString());
            newCart.setUser(user);
            return cartRepository.saveAndFlush(newCart);
        });

        Optional<CartItem> existingItemOpt = cart.getCartItems().stream()
                .filter(i -> !i.isDeleted()
                        && CartStatus.ACTIVE.getCode().equalsIgnoreCase(i.getStatus())
                        && i.getProductVariant() != null
                        && request.getProductId().equals(i.getProductVariant().getId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (newQuantity > product.getQuantity()) {
                return CommonResponse.builder()
                        .code(CommonResponse.CODE_NOT_FOUND)
                        .message("Số lượng sản phẩm không đủ trong kho")
                        .build();
            }
            existingItem.setQuantity(newQuantity);
            existingItem.setAmount(product.getPrice().multiply(BigDecimal.valueOf(newQuantity)));
            cartItemRepository.saveAndFlush(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setId(UUID.randomUUID().toString());
            newItem.setCart(cart);
            newItem.setProductVariant(product);
            newItem.setQuantity(request.getQuantity());
            newItem.setStatus(CartStatus.ACTIVE.getCode());
            newItem.setAmount(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
            cartItemRepository.saveAndFlush(newItem);
        }

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .message("Thêm sản phẩm vào giỏ hàng thành công")
                .build();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResponse updateCartItem(CartRequest request) {
        CartItem item = cartItemRepository.findById(request.getIdCartItem()).orElse(null);
        if (item == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Không tìm thấy sản phẩm trong giỏ")
                    .build();
        }

        ProductVariant product = productVariantRepository.findById(item.getProductVariant().getId()).orElse(null);
        if (product == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Không tìm thấy sản phẩm")
                    .build();
        }

        if (request.getQuantity() > product.getQuantity()) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Số lượng sản phẩm không đủ trong kho")
                    .build();
        }

        if (request.getQuantity() <= 0) {
            item.setDeleted(true);
            cartItemRepository.saveAndFlush(item);
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_SUCCESS)
                    .message("Xóa sản phẩm khỏi giỏ hàng thành công")
                    .build();
        }

        item.setQuantity(request.getQuantity());
        item.setAmount(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        cartItemRepository.saveAndFlush(item);

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .message("Cập nhật sản phẩm trong giỏ hàng thành công")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CommonResponse getCartItems(CartSearch search) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_ACCOUNT_EXCEPTION)
                    .message("Vui lòng đăng nhập")
                    .build();
        }

        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .data(new ArrayList<>())
                    .message("Giỏ hàng trống")
                    .build();
        }

        CartResponse response = CartResponse.fromCart(cart);
        if (response.getProducts() == null || response.getProducts().isEmpty()) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .data(new ArrayList<>())
                    .message("Giỏ hàng trống")
                    .build();
        }

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(response)
                .message("Lấy giỏ hàng thành công")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResponse checkoutCart(CheckOutRequest checkOutRequest) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_ACCOUNT_EXCEPTION)
                    .message("Vui lòng đăng nhập")
                    .build();
        }

        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Giỏ hàng trống")
                    .build();
        }

        java.util.List<CartItem> activeItems = cart.getCartItems().stream()
                .filter(item -> !item.isDeleted() && CartStatus.ACTIVE.getCode().equalsIgnoreCase(item.getStatus()))
                .toList();

        if (activeItems.isEmpty()) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Giỏ hàng không có sản phẩm")
                    .build();
        }

        for (CartItem item : activeItems) {
            if (item.getProductVariant().getQuantity() < item.getQuantity()) {
                return CommonResponse.builder()
                        .code(CommonResponse.CODE_NOT_FOUND)
                        .message("Số lượng sản phẩm không đủ trong kho")
                        .build();
            }
        }

        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setOrderCode(Utils.generateUniqueCode("ORDER-"));
        order.setCustomer(user);
        order.setTotalAmount(activeItems.stream()
                .map(i -> i.getProductVariant().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
        );
        order.setStatus(OrderStatus.PENDING.getCode());
        order.setAddressDetail(checkOutRequest.getAddressLine());
        order.setMethodTransaction("THANH TOÁN KHI NHẬN HÀNG");
        orderRepository.save(order);

        for (CartItem item : activeItems) {
            ProductVariant p = item.getProductVariant();
            p.setQuantity(p.getQuantity() - item.getQuantity());
            productVariantRepository.saveAndFlush(p);

            OrderItem orderItem = new OrderItem();
            orderItem.setId(UUID.randomUUID().toString());
            orderItem.setOrder(order);
            orderItem.setProductVariant(item.getProductVariant());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(item.getProductVariant().getPrice());
            orderItemRepository.saveAndFlush(orderItem);

            item.setStatus(CartStatus.CHECKED_OUT.getCode());
            item.setDeleted(true);
            cartItemRepository.saveAndFlush(item);
        }
        OrderStatusHistory orderStatusHistory = new OrderStatusHistory();
        orderStatusHistory.setId(UUID.randomUUID().toString());
        orderStatusHistory.setOrder(order);
        orderStatusHistory.setStatus(OrderStatus.PENDING.getCode());
        orderStatusHistoryRepository.saveAndFlush(orderStatusHistory);

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .message("Thanh toán thành công")
                .data(order)
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
