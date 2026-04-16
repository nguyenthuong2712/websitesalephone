package org.example.websitesalephone.dto.shop;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ShopRegisterRequest {
    private String username;
    private String shopName;
    private String description;
    private List<ShopPaymentMethodRequest> paymentMethods;
}
