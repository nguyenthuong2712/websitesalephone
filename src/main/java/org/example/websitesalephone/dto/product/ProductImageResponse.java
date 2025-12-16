package org.example.websitesalephone.dto.product;

import lombok.Getter;
import lombok.Setter;
import org.example.websitesalephone.entity.ProductImage;

@Getter
@Setter
public class ProductImageResponse {

    private String productImageId;

    private String url;

    private boolean isActive;

    public static ProductImageResponse fromEntity(ProductImage productImage) {
        ProductImageResponse productImageResponse = new ProductImageResponse();
        productImageResponse.setProductImageId(productImage.getId());
        productImageResponse.setUrl(productImage.getUrl());
        productImageResponse.setActive(productImage.isActive());
        return productImageResponse;
    }
}
