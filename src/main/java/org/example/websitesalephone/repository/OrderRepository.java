package org.example.websitesalephone.repository;

import org.example.websitesalephone.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

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
          AND o.IS_DELETED = 0
    """, nativeQuery = true)
    BigDecimal getRevenueByStatus();

    @Query(value = """
        SELECT COUNT(DISTINCT o.id)
        FROM orders o
        JOIN order_items oi ON oi.order_id = o.id AND oi.IS_DELETED = 0
        JOIN product_variants pv ON pv.id = oi.product_variant_id AND pv.IS_DELETED = 0
        JOIN products p ON p.id = pv.product_id AND p.IS_DELETED = 0
        JOIN shop_registrations sr ON sr.id = p.shop_registration_id AND sr.IS_DELETED = 0
        WHERE o.IS_DELETED = 0
          AND sr.user_id = :sellerId
          AND (:status = 'ALL' OR o.status = :status)
    """, nativeQuery = true)
    int countBySellerIdAndStatus(@Param("sellerId") String sellerId, @Param("status") String status);

    @Query(value = """
        SELECT COALESCE(SUM(oi.unit_price * oi.quantity), 0)
        FROM orders o
        JOIN order_items oi ON oi.order_id = o.id AND oi.IS_DELETED = 0
        JOIN product_variants pv ON pv.id = oi.product_variant_id AND pv.IS_DELETED = 0
        JOIN products p ON p.id = pv.product_id AND p.IS_DELETED = 0
        JOIN shop_registrations sr ON sr.id = p.shop_registration_id AND sr.IS_DELETED = 0
        WHERE o.IS_DELETED = 0
          AND o.status = 'COMPLETED'
          AND sr.user_id = :sellerId
    """, nativeQuery = true)
    BigDecimal getRevenueBySellerId(@Param("sellerId") String sellerId);
}
