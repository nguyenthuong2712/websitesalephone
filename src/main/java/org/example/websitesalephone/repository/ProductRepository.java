package org.example.websitesalephone.repository;

import org.example.websitesalephone.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {

    Page<Product> findAll(Specification specification, Pageable pageable);

    long countByIsDeletedFalseAndVariantsIsNotEmpty();

    List<Product> findTop8ByOrderByCreatedAtDesc();

}
