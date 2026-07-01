package org.example.websitesalephone.dto.cart;

import lombok.Data;

@Data
public class BuyNowCartRequest {
    private String variantId;
    private Integer quantity;
    private String addressLine;
    private String paymentMethod; // VNPAY or COD
}
