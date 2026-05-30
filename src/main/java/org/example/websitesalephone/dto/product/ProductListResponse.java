package org.example.websitesalephone.dto.product;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.example.websitesalephone.entity.Product;
import org.example.websitesalephone.entity.ProductImage;
import org.example.websitesalephone.entity.ProductVariant;
import org.example.websitesalephone.enums.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@FieldNameConstants
public class ProductListResponse {

    private String id;

    private String url;

    private String productName;

    private String originName;

    private BigDecimal price;

    private int quantityUnitSold;

    private int quantity;

    private String status;

    private List<ProductImageResponse> responseList;

    public static ProductListResponse fromEntity(Product entity) {
        List<ProductVariant> variants = entity.getVariants() == null ? List.of() : entity.getVariants();

        int totalQuantity = variants.stream()
                .mapToInt(ProductVariant::getQuantity)
                .sum();

        int totalQuantitySold = variants.stream()
                .mapToInt(ProductVariant::getQuantityUnitSold)
                .sum();

//        BigDecimal averagePrice = BigDecimal.ZERO;

//        if (!entity.getVariants().isEmpty()) {
//            averagePrice = entity.getVariants().stream()
//                    .map(ProductVariant::getPrice)
//                    .reduce(BigDecimal.ZERO, BigDecimal::add)
//                    .divide(new BigDecimal(entity.getVariants().size()), 2, RoundingMode.HALF_UP);
//        }

        String imageUrl = entity.getImages().stream()
                .filter(image -> !image.isDeleted())
                .sorted((left, right) -> Boolean.compare(right.isActive(), left.isActive()))
                .map(ProductImage::getUrl)
                .findFirst()
                .orElse(null);

        String statusCode = entity.getStatus() == null ? null : entity.getStatus().getCode();
        if (totalQuantity <= 0 && ProductStatus.ACTIVE.getCode().equals(statusCode)) {
            statusCode = ProductStatus.OUT_OF_STOCK.getCode();
        }

        return ProductListResponse.builder()
                .id(entity.getId())
                .url(imageUrl)
                .productName(entity.getName())
                .originName(variants.isEmpty() ? null : variants.getFirst().getOrigin().getName())
                .price(entity.getPrice())
                .quantity(totalQuantity)
                .quantityUnitSold(totalQuantitySold)
                .status(statusCode)
                .build();
    }
}
