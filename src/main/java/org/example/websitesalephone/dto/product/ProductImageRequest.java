package org.example.websitesalephone.dto.product;

import lombok.Getter;

@Getter
public class ProductImageRequest {

    private String productImageId;

    private String productId;

    private String url;

    private boolean isActive;

}
