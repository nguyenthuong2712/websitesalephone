package org.example.websitesalephone.repository;

import org.example.websitesalephone.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, String> {

    List<ProductImage> findByProduct_idAndIsDeleted(String productId, boolean isDeleted);

    ProductImage findByActiveAndId(boolean active, String id);
}
