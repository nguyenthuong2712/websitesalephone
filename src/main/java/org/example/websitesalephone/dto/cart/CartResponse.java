package org.example.websitesalephone.dto.cart;

import lombok.Builder;
import lombok.Getter;
import org.example.websitesalephone.entity.*;
import org.example.websitesalephone.enums.CartStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Getter
@Builder
public class CartResponse {

    private List<ProductInCart> products;

    private int totalQuantity;

    private BigDecimal total;

    public static CartResponse fromCart(Cart cart) {
        if (cart == null || cart.getCartItems() == null) {
            return CartResponse.builder().products(List.of()).totalQuantity(0).total(BigDecimal.ZERO).build();
        }
        return fromCartItems(cart.getCartItems().stream().filter(item -> !item.isDeleted()).toList());
    }

    public static CartResponse fromCartItems(List<CartItem> activeItems) {
        if (activeItems == null) {
            return CartResponse.builder().products(List.of()).totalQuantity(0).total(BigDecimal.ZERO).build();
        }
        List<ProductInCart> productList = activeItems.stream()
                .filter(item -> item.getStatus().equalsIgnoreCase(CartStatus.ACTIVE.getCode()))
                .map(item -> {
                    ProductVariant variant = item.getProductVariant();
                    Product product = variant.getProduct();

                    String imageUrl = product.getImages() == null ? null : product.getImages().stream()
                            .filter(image -> !image.isDeleted())
                            .sorted((left, right) -> Boolean.compare(right.isActive(), left.isActive()))
                            .map(ProductImage::getUrl)
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(null);

                    return ProductInCart.builder()
                            .idCartItem(item.getId())
                            .productId(variant.getId())
                            .productName(Objects.requireNonNull(product).getName())
                            .quantity(item.getQuantity())
                            .ram(variant.getRam().getName())
                            .color(variant.getColor().getName())
                            .origin(variant.getOrigin().getName())
                            .image(imageUrl)
                            .price(variant.getPrice())
                            .build();
                })
                .toList();

        int totalQuantity = productList.stream()
                .mapToInt(ProductInCart::getQuantity)
                .sum();

        BigDecimal total = productList.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .products(productList)
                .totalQuantity(totalQuantity)
                .total(total)
                .build();
    }

}
