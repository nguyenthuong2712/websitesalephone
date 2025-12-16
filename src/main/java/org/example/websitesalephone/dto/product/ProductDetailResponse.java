package org.example.websitesalephone.dto.product;

import lombok.Builder;
import lombok.Getter;
import org.example.websitesalephone.dto.dynamic.DynamicResponse;
import org.example.websitesalephone.entity.Product;
import org.example.websitesalephone.entity.ProductImage;
import org.example.websitesalephone.entity.ProductVariant;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class ProductDetailResponse {

    private String id;
    private String productName;
    private String description;
    private BigDecimal price;
    private List<DynamicResponse> colors;
    private List<DynamicResponse> cameras;
    private List<DynamicResponse> batterys;
    private List<DynamicResponse> cpus;
    private List<DynamicResponse> images;
    private List<DynamicResponse> screens;
    private List<DynamicResponse> origins;
    private List<DynamicResponse> storages;
    private List<DynamicResponse> operators;
    private List<DynamicResponse> rams;
    private List<ProductImageResponse> responseList;

    public static ProductDetailResponse fromEntity(Product product, List<ProductVariant> productVariants, List<ProductImage> productImages) {
        if (product == null || productVariants == null || productVariants.isEmpty()) {
            return null;
        }

        ProductVariant variant = productVariants.get(0);

        List<ProductImageResponse> responseList = product.getImages().stream().map(ProductImageResponse::fromEntity).toList();

        return ProductDetailResponse.builder()
                .id(product.getId())
                .productName(product.getName())
                .description(product.getDescription())
                .price(variant.getPrice())
                .responseList(responseList.isEmpty() ? null : responseList)
                .colors(removeDuplicate(productVariants.stream()
                        .map(v -> new DynamicResponse(v.getColor().getId(), v.getColor().getName()))
                        .toList()))
//                .batterys(removeDuplicate(productVariants.stream()
//                        .map(v -> new DynamicResponse(v.getBattery().getId(), v.getBattery().getName()))
//                        .toList()))
//                .cpus(removeDuplicate(productVariants.stream()
//                        .map(v -> new DynamicResponse(v.getCpu().getId(), v.getCpu().getName()))
//                        .toList()))
                .rams(removeDuplicate(productVariants.stream()
                        .map(v -> new DynamicResponse(v.getRam().getId(), v.getRam().getName()))
                        .toList()))
//                .storages(removeDuplicate(productVariants.stream()
//                        .map(v -> new DynamicResponse(v.getStorage().getId(), v.getStorage().getName()))
//                        .toList()))
//                .operators(removeDuplicate(productVariants.stream()
//                        .map(v -> new DynamicResponse(v.getOperatingSystem().getId(), v.getOperatingSystem().getName()))
//                        .toList()))
                .origins(removeDuplicate(productVariants.stream()
                        .map(v -> new DynamicResponse(v.getOrigin().getId(), v.getOrigin().getName()))
                        .toList()))
                .cameras(removeDuplicate(productVariants.stream()
                        .map(v -> new DynamicResponse(v.getCamera().getId(), v.getCamera().getName()))
                        .toList()))
                .screens(removeDuplicate(productVariants.stream()
                        .map(v -> new DynamicResponse(v.getScreen().getId(), v.getScreen().getName()))
                        .toList()))
                .images(removeDuplicate(productImages.stream()
                        .map(v -> new DynamicResponse(v.getId(), v.getUrl()))
                        .toList()))
                .build();
    }

    private static List<DynamicResponse> removeDuplicate(List<DynamicResponse> list) {
        return list.stream()
                .filter(distinctByKey(DynamicResponse::getId))
                .toList();
    }

    private static <T> java.util.function.Predicate<T> distinctByKey(java.util.function.Function<? super T, ?> keyExtractor) {
        java.util.Set<Object> seen = new java.util.HashSet<>();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
