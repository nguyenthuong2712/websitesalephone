package org.example.websitesalephone.dto.product;

import lombok.Builder;
import lombok.Getter;
import org.example.websitesalephone.entity.ProductVariant;

import java.math.BigDecimal;

@Getter
@Builder
public class ProductVariantResponse {

    private String idProduct;

    private String productName;

    private String description;

    private int quantity;

    private String ramId;
    private String colorId;
    private String originId;
    private String screenId;
    private String cameraId;

    private String colorName;

    private String originName;

    private String screenName;

    private String cameraName ;

    private String ramName;

    private BigDecimal price;

    public static ProductVariantResponse from(ProductVariant productVariant) {
        return ProductVariantResponse
                .builder()
                .idProduct(productVariant.getId())
                .quantity(productVariant.getQuantity())
                .price(productVariant.getPrice())
                .ramId(productVariant.getRam().getId())
                .colorId(productVariant.getColor().getId())
                .originId(productVariant.getOrigin().getId())
                .screenId(productVariant.getScreen().getId())
                .cameraId(productVariant.getCamera().getId())
                .build();
    }

    public static ProductVariantResponse fromEntity(ProductVariant productVariant) {
        return ProductVariantResponse
                .builder()
                .idProduct(productVariant.getId())
                .productName(productVariant.getProduct().getName())
                .description(productVariant.getProduct().getDescription())
                .quantity(productVariant.getQuantity())
                .price(productVariant.getPrice())
                .originName(productVariant.getOrigin().getName())
                .colorName(productVariant.getColor().getName())
                .ramName(productVariant.getRam().getName())
                .cameraName(productVariant.getCamera().getName())
                .screenName(productVariant.getScreen().getName())
                .ramId(productVariant.getRam().getId())
                .colorId(productVariant.getColor().getId())
                .originId(productVariant.getOrigin().getId())
                .screenId(productVariant.getScreen().getId())
                .cameraId(productVariant.getCamera().getId())
                .build();
    }
}
