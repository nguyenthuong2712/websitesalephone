package org.example.websitesalephone.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.websitesalephone.entity.Order;
import org.example.websitesalephone.entity.OrderItem;
import org.example.websitesalephone.utils.Constants;
import org.example.websitesalephone.utils.Utils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

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

        int totalQuantity = order.getOrderItems().stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        BigDecimal totalPrice = order.getOrderItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOrderAmount = totalPrice.add(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO);

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
                .shippingFee(order.getShippingFee())
                .totalOrderAmount(totalOrderAmount)
                    .codeStaff(order.getStaff() == null ? "ADMIN" : order.getStaff().getCodeUser())
                .build();
    }

}
