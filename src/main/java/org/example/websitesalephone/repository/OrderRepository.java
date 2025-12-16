package org.example.websitesalephone.repository;

import org.example.websitesalephone.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);
    List<Order> findAll(Specification<Order> spec);
    int countByStatus(String status);
    int countAllByIsDeletedFalse();
    int countByStatusAndCustomer_Id(String status, String customerId);
    int countByCustomer_Id(String customerId);
    int countByStaff_Id(String customerId);
    int countByStatusAndStaff_Id(String status, String staffId);

    @Query(value = """
        SELECT COALESCE(SUM(o.total_amount), 0)s
        FROM orders o
        WHERE o.status = 'COMPLETED'
    """, nativeQuery = true)
    BigDecimal getRevenueByStatus();
}
