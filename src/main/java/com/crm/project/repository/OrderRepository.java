package com.crm.project.repository;

import com.crm.project.entity.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.crm.project.internal.OrderStatisticInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    @Query("""
                SELECT DISTINCT o 
                FROM Order o
                LEFT JOIN FETCH o.orderItems
                LEFT JOIN FETCH o.createdBy
                LEFT JOIN FETCH o.lead
                WHERE o.id = :id
            """)
    Optional<Order> findOrderWithRelations(String id);

    @Query("SELECT o.id FROM Order o")
    Page<String> findAllIds(Pageable pageable);

    @Query("""
                SELECT DISTINCT o 
                FROM Order o
                LEFT JOIN FETCH o.orderItems
                LEFT JOIN FETCH o.createdBy
                LEFT JOIN FETCH o.lead
                WHERE o.id IN :ids
            """)
    List<Order> findAllOrdersWithDetails(List<String> ids);

    @Query("""
                SELECT o.id 
                FROM Order o
                LEFT JOIN o.orderItems
                LEFT JOIN o.createdBy
                LEFT JOIN o.lead l
                WHERE LOWER(l.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<String> findIdsBySearch(String query, Pageable pageable);

    boolean existsByOrderCode(String orderCode);

    boolean existsByQuotationId(String quotationId);

    @Query(value = "SELECT status, COUNT(*) AS total FROM orders GROUP BY status", nativeQuery = true)
    List<Map<String, Object>> countByStatus();

    @Query("SELECT SUM(o.totalAmount) FROM Order o")
    BigDecimal sumTotalAmount();

    @Modifying
    @Query("UPDATE Order o SET o.status = 'Cancelled' WHERE o.id = :id")
    void updateStatusToCancelled(@Param("id") String id);

    @Query("""
            SELECT new com.crm.project.internal.OrderStatisticInfo(
                COUNT(o),
                SUM(o.totalAmount),
                AVG(o.totalAmount),
                MAX(o.createdAt)
            )
            FROM Order o
            WHERE o.lead.id = :customerId
            """)
    OrderStatisticInfo getOrderStatisticsByCustomer(String customerId);

    // Chart queries for Order Revenue
    @Query("""
            SELECT 
                FUNCTION('DAYNAME', o.createdAt) as dayName,
                COALESCE(SUM(o.totalAmount), 0) as totalRevenue
            FROM Order o
            WHERE o.createdAt >= :startDate AND o.createdAt < :endDate
            GROUP BY FUNCTION('DAYOFWEEK', o.createdAt), FUNCTION('DAYNAME', o.createdAt)
            ORDER BY FUNCTION('DAYOFWEEK', o.createdAt)
            """)
    List<Object[]> getRevenueByDayOfWeek(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate
    );

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0)
            FROM Order o
            WHERE o.createdAt >= :startDate AND o.createdAt < :endDate
            """)
    BigDecimal getTotalRevenueByPeriod(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate
    );

    // Count total orders
    @Query("SELECT COUNT(o) FROM Order o")
    Long countTotalOrders();


}
