package org.example.websitesalephone.dto.shop;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopPaymentMethodRequest {
    private String method;
    private String qrCode;
    private String note;
}
