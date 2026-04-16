package org.example.websitesalephone.dto.cart;

import lombok.Builder;
import lombok.Getter;
import org.example.websitesalephone.entity.*;
import org.example.websitesalephone.enums.CartStatus;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter
@Builder
public class CartResponse {

    private List<ProductInCart> products;

    private int totalQuantity;

    private BigDecimal total;

    public static CartResponse fromCart(Cart cart) {
        List<ProductInCart> productList = cart.getCartItems().stream()
                .filter(item -> !item.isDeleted() && item.getStatus().equalsIgnoreCase(CartStatus.ACTIVE.getCode()))
                .map(item -> {
                    ProductVariant variant = item.getProductVariant();
                    Product product = variant.getProduct();

                    String imageUrl = product.getImages().stream()
                            .filter(ProductImage::isActive)
                            .findFirst()
                            .map(ProductImage::getUrl)
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

        int totalQuantity = cart.getCartItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        BigDecimal total = cart.getCartItems().stream()
                .map(item -> item.getProductVariant().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .products(productList)
                .totalQuantity(totalQuantity)
                .total(total)
                .build();
    }

}
