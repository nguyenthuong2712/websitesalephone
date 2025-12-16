package org.example.websitesalephone.dto.order;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderRequest {

    private String id;

    private String status;

    private BigDecimal shippingFee;

    private String description;
}
