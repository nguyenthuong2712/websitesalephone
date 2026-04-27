package org.example.websitesalephone.repository;

import org.example.websitesalephone.entity.Product;
import org.example.websitesalephone.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {

    Page<Product> findAll(Specification specification, Pageable pageable);

    long countByVariantsIsNotEmpty();

    @Query(value = """
            SELECT COUNT(DISTINCT p.id)
            FROM products p
            JOIN product_variants pv ON pv.product_id = p.id AND pv.IS_DELETED = 0
            JOIN shop_registrations sr ON sr.id = p.shop_registration_id AND sr.IS_DELETED = 0
            WHERE p.IS_DELETED = 0
              AND sr.user_id = :sellerId
            """, nativeQuery = true)
    long countSellableProductsBySellerId(@Param("sellerId") String sellerId);

    List<Product> findTop8ByIsDeletedFalseAndStatusOrderByCreatedAtDesc(ProductStatus status);

}
