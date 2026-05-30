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

    long countByIsDeletedFalseAndVariantsIsNotEmpty();

    long countByIsDeletedFalse();

    @Query("""
            select distinct p
            from Product p
            join p.variants v
            where p.isDeleted = false
              and p.status = :status
              and v.isDeleted = false
              and v.quantity > 0
            order by p.createdAt desc
            """)
    List<Product> findHomeProducts(@Param("status") ProductStatus status, Pageable pageable);

}
