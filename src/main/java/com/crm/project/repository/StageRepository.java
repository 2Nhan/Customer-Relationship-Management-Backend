package com.crm.project.repository;

import com.crm.project.entity.Stage;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StageRepository extends JpaRepository<Stage, String> {
    boolean existsStageByName(String name);

    Optional<Stage> findByName(String name);

    @Query("SELECT DISTINCT s FROM Stage s LEFT JOIN FETCH s.leads l LEFT JOIN FETCH l.user ORDER BY s.rankOrder ASC")
    List<Stage> findAllWithLeads();

    @Query("SELECT MAX(s.rankOrder) FROM Stage s WHERE s.name NOT IN ('Won', 'Lost')")
    Optional<Integer> findMaxRankOrder();

    @Query("""
            SELECT DISTINCT s FROM Stage s
            LEFT JOIN FETCH s.leads l
            WHERE (:query IS NULL 
                   OR LOWER(l.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(l.email) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(l.phoneNumber) LIKE LOWER(CONCAT('%', :query, '%')))
            ORDER BY s.rankOrder ASC
            """)
    List<Stage> searchLeadsGroupedByStage(@Param("query") String search);
}
