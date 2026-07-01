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
    Optional<Order> findByPayosOrderCode(Long payosOrderCode);

    Page<Order> findAll(Specification<Order> spec, Pageable pageable);
    List<Order> findAll(Specification<Order> spec);
    int countByStatus(String status);
    int countAllByIsDeletedFalse();
    int countByStatusAndCustomer_Id(String status, String customerId);
    int countByCustomer_Id(String customerId);
    int countByStaff_Id(String customerId);
    int countByStatusAndStaff_Id(String status, String staffId);

    @Query(value = """
        SELECT COALESCE(SUM(o.total_amount), 0)
        FROM orders o
        WHERE o.status = 'COMPLETED'
    """, nativeQuery = true)
    BigDecimal getRevenueByStatus();

    @Query(value = """
        SELECT COALESCE(SUM(o.total_amount), 0)
        FROM orders o
        WHERE o.status = 'COMPLETED'
          AND o.created_at >= :startDate
          AND o.created_at <= :endDate
    """, nativeQuery = true)
    BigDecimal getRevenueByStatusAndDateRange(@Param("startDate") java.time.OffsetDateTime startDate, @Param("endDate") java.time.OffsetDateTime endDate);
}
