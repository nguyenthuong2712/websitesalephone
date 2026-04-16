package org.example.websitesalephone.repository;

import org.example.websitesalephone.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, String> {
    boolean existsByProductVariant_IdIn(List<String> productVariantIds);

    void deleteByProductVariant_IdIn(List<String> productVariantIds);

}
