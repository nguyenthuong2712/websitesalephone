package org.example.websitesalephone.dto.cart;

import lombok.Getter;

@Getter
public class CartRequest {
    private String idCartItem;
    private String productId;
    private int quantity;
}
