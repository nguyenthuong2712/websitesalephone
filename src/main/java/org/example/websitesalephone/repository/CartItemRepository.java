package org.example.websitesalephone.repository;

import org.example.websitesalephone.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, String> {
    boolean existsByProductVariant_IdIn(List<String> productVariantIds);

    void deleteByProductVariant_IdIn(List<String> productVariantIds);

}
