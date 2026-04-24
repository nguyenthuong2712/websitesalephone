package org.example.websitesalephone.dto.product;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductDetailRequest {

    private String productName;

    private String description;

    private String location;

    private String storage;

    private String deviceMake;

    private String status;

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
