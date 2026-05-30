package org.example.websitesalephone.dto.product;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductDetailRequest {

    private String productName;

    private String description;

    private int quantity;

    private BigDecimal price;

    private String productVariantId;

    private String idProduct;

    private String colorId;

    private String cameraId;

    private String imageId;

    private String screenId;

    private String originId;

    private String ramId;
}
