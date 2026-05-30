package org.example.websitesalephone.dto.order;

import lombok.Builder;
import lombok.Getter;
import org.example.websitesalephone.entity.Address;
import org.example.websitesalephone.entity.Order;
import org.example.websitesalephone.entity.ProductImage;
import org.example.websitesalephone.utils.Constants;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class OrderDetailResponse {

    private String orderCode;

    private String createdAt;

    private String status;

    private String fullName;

    private String phoneNumber;

    private String address;

    private String methodTransaction;

    private BigDecimal totalPrice;

    private BigDecimal totalOrderAmount;

    private String statusTransaction;

    private BigDecimal shippingFee;

    private String email;

    private List<ProductOrderResponse> productOrderResponses;

    private List<OrderHistoryStatusResponse> orderHistoryStatusResponses;

    public static OrderDetailResponse fromEntity(Order order) {

        List<ProductOrderResponse> productOrderResponses = order.getOrderItems().stream()
                .map(item -> {

                    String imageUrl = item.getProductVariant().getProduct().getImages().stream()
                            .filter(ProductImage::isActive)
                            .findFirst()
                            .map(ProductImage::getUrl)
                            .orElse(null);

                    ProductOrderResponse productDTO = new ProductOrderResponse();
                    productDTO.setProductName(item.getProductVariant().getProduct().getName());
                    productDTO.setImage(imageUrl);
                    productDTO.setRam(item.getProductVariant().getRam().getName());
                    productDTO.setColor(item.getProductVariant().getColor().getName());
                    productDTO.setScreen(item.getProductVariant().getScreen().getName());
                    productDTO.setCamera(item.getProductVariant().getCamera().getName());
                    productDTO.setProductName(item.getProductVariant().getProduct().getName());
                    productDTO.setProductPrice(item.getUnitPrice());
                    productDTO.setQuantity(item.getQuantity());
                    productDTO.setIntoMoney(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    return productDTO;
                })
                .toList();

        BigDecimal totalPrice = productOrderResponses.stream()
                .map(ProductOrderResponse::getIntoMoney)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OrderHistoryStatusResponse> orderHistoryStatusResponseList = order.getStatusHistories()
                .stream().map(item -> {
                    OrderHistoryStatusResponse orderHistoryStatusResponse = new OrderHistoryStatusResponse();
                    orderHistoryStatusResponse.setStatus(item.getStatus());
                    orderHistoryStatusResponse.setCreatedAt(Constants.FORMATTER.format(item.getCreatedAt()));
                    return orderHistoryStatusResponse;
                }).toList();

        BigDecimal totalOrderAmount = totalPrice.add(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO);

        return OrderDetailResponse.builder()
                .orderCode(order.getOrderCode())
                .createdAt(Constants.FORMATTER.format(order.getCreatedAt()))
                .status(order.getStatus() != null ? order.getStatus() : null)
                .fullName(order.getCustomer() != null ? order.getCustomer().getFullName() : null)
                .phoneNumber(order.getCustomer() != null ? order.getCustomer().getPhone() : null)
                .address(order.getAddressDetail() == null ? "" : order.getAddressDetail())
                .methodTransaction(order.getMethodTransaction())
                .totalPrice(totalPrice)
                .totalOrderAmount(totalOrderAmount)
                .shippingFee(order.getShippingFee())
                .email(order.getCustomer().getEmail())
                .statusTransaction(order.getStatusTransaction())
                .productOrderResponses(productOrderResponses)
                .orderHistoryStatusResponses(orderHistoryStatusResponseList)
                .build();
    }

}
