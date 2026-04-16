package org.example.websitesalephone.dto.product;

import lombok.Getter;
import lombok.Setter;
import org.example.websitesalephone.entity.ProductVariant;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductVariantDetailResponse {

    private String productVariantId;

    private int quantity;

    private BigDecimal price;

    private String ramId;

    private String cameraId;

    private String screenId;

    private String originId;

    private String colorId;

    public static ProductVariantDetailResponse from(ProductVariant productVariant) {
        ProductVariantDetailResponse response = new ProductVariantDetailResponse();
        response.setProductVariantId(productVariant.getId());
        response.setPrice(productVariant.getPrice());
        response.setQuantity(productVariant.getQuantity());
        response.setCameraId(productVariant.getCamera().getId());
        response.setColorId(productVariant.getColor().getId());
        response.setScreenId(productVariant.getScreen().getId());
        response.setRamId(productVariant.getRam().getId());
        response.setOriginId(productVariant.getOrigin().getId());
        return response;
    }
}
