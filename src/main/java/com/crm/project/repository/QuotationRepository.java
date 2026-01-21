package com.crm.project.repository;

import com.crm.project.entity.Quotation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, String> {
    @Query("""
                SELECT q FROM Quotation q
                LEFT JOIN FETCH q.lead
                LEFT JOIN FETCH q.items i
                LEFT JOIN FETCH i.product
                WHERE q.id = :id
            """)
    Optional<Quotation> findQuotationDetailById(String id);

    @Query("SELECT q.id FROM Quotation q")
    Page<String> findAllQuotationIds(Pageable pageable);

    @Query("""
                SELECT DISTINCT q FROM Quotation q
                LEFT JOIN FETCH q.lead
                LEFT JOIN FETCH q.createdBy
                LEFT JOIN FETCH q.items i
                LEFT JOIN FETCH i.product
                WHERE q.id IN :ids
            """)
    List<Quotation> findAllQuotationsWithDetails(List<String> ids);

    @Query("""
                SELECT q.id
                FROM Quotation q
                LEFT JOIN q.createdBy cb
                LEFT JOIN q.lead l
                LEFT JOIN q.items i
                LEFT JOIN i.product
                WHERE LOWER(l.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(q.title) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<String> findQuotationsBySearch(String query, Pageable pageable);

    @Modifying
    @Query("""
                UPDATE Quotation q 
                SET q.status = 'Expired' 
                WHERE q.validUntil < CURRENT_DATE 
                  AND q.status = 'Draft'
            """)
    int markExpiredQuotations();

    @Query(value = "SELECT status, COUNT(*) AS total FROM quotations GROUP BY status", nativeQuery = true)
    List<Map<String, Object>> countByStatus();

    @Query("""
            SELECT 
                FUNCTION('DAYNAME', q.createdAt) as dayName,
                COALESCE(SUM(q.finalTotal), 0) as totalRevenue
            FROM Quotation q
            WHERE q.createdAt >= :startDate AND q.createdAt < :endDate
            GROUP BY FUNCTION('DAYOFWEEK', q.createdAt), FUNCTION('DAYNAME', q.createdAt)
            ORDER BY FUNCTION('DAYOFWEEK', q.createdAt)
            """)
    List<Object[]> getRevenueByDayOfWeek(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate
    );

    @Query("""
            SELECT COALESCE(SUM(q.finalTotal), 0)
            FROM Quotation q
            WHERE q.createdAt >= :startDate AND q.createdAt < :endDate
            """)
    BigDecimal getTotalRevenueByPeriod(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate
    );

    // Count total quotations
    @Query("SELECT COUNT(q) FROM Quotation q")
    Long countTotalQuotations();
}
