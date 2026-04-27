package org.example.websitesalephone.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.websitesalephone.entity.Order;
import org.example.websitesalephone.entity.OrderItem;
import org.example.websitesalephone.utils.Constants;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Getter
@AllArgsConstructor
@Builder
public class OrderResponse {

    private String order_id;

    private String orderCode;

    private String userName;

    private String phone;

    private String createdAt; // đổi từ OffsetDateTime -> String

    private String dateTimeCheckout; // đổi từ OffsetDateTime -> String

    private int quantity;

    private String status;

    private BigDecimal totalPrice;

    private BigDecimal shippingFee;

    private BigDecimal totalOrderAmount;

    private String codeStaff;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");

    public static OrderResponse fromOrder(final Order order) {
        return fromOrder(order, null);
    }

    public static OrderResponse fromSellerOrder(final Order order, final String sellerId) {
        return fromOrder(order, sellerId);
    }

    private static OrderResponse fromOrder(final Order order, final String sellerId) {
        List<OrderItem> orderItems = order.getOrderItems() == null ? List.of() : order.getOrderItems();
        List<OrderItem> scopedItems = sellerId == null
                ? orderItems
                : orderItems.stream().filter(item -> belongsToSeller(item, sellerId)).toList();

        int totalQuantity = scopedItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        BigDecimal totalPrice = scopedItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = sellerId == null && order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO;
        BigDecimal totalOrderAmount = totalPrice.add(shippingFee);

        return OrderResponse.builder()
                .order_id(order.getId())
                .orderCode(order.getOrderCode())
                .userName(order.getCustomer() != null ? order.getCustomer().getFullName() : null)
                .phone(order.getCustomer() != null ? order.getCustomer().getPhone() : null)
                .createdAt(Constants.FORMATTER.format(order.getCreatedAt()))
                .dateTimeCheckout(order.getDateTimeCheckout() != null ? order.getDateTimeCheckout().format(FORMATTER) : null)
                .quantity(totalQuantity)
                .status(order.getStatus())
                .totalPrice(totalPrice)
                .shippingFee(shippingFee)
                .totalOrderAmount(totalOrderAmount)
                .codeStaff(order.getStaff() == null ? "ADMIN" : order.getStaff().getCodeUser())
                .build();
    }

    private static boolean belongsToSeller(final OrderItem item, final String sellerId) {
        return item.getProductVariant() != null
                && item.getProductVariant().getProduct() != null
                && item.getProductVariant().getProduct().getShopRegistration() != null
                && item.getProductVariant().getProduct().getShopRegistration().getUser() != null
                && Objects.equals(item.getProductVariant().getProduct().getShopRegistration().getUser().getId(), sellerId);
    }

}
