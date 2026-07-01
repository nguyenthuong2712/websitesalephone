package org.example.websitesalephone.dto.order;

import lombok.Data;

@Data
public class BuyNowRequest {
    private String variantId;
    private Integer quantity;
    private String addressLine;
    private String paymentMethod; // VNPAY or COD
}
