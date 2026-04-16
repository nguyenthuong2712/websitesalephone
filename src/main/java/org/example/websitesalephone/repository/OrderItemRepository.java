package org.example.websitesalephone.repository;

import org.example.websitesalephone.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    boolean existsByProductVariant_IdIn(List<String> productVariantIds);

}
