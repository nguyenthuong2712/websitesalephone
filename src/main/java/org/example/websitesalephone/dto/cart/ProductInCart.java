package org.example.websitesalephone.dto.cart;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ProductInCart {

    private String idCartItem;

    private String productId;

    private String productName;

    private int quantity;

    private String ram;

    private String origin;

    private String color;

    private String image;

    private BigDecimal price;
}
