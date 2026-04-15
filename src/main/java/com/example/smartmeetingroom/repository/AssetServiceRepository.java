package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.dto.assetservice.AssetServiceDTO;
import com.example.smartmeetingroom.entity.AssetService;
import com.example.smartmeetingroom.enums.AssetServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AssetServiceRepository extends JpaRepository<AssetService, Long> {
    boolean existsByAssetIdAndStatusIn(Long assetId, Collection<AssetServiceStatus> statuses);

    @Query("""
        SELECT new com.example.smartmeetingroom.dto.assetservice.AssetServiceDTO(
            s.id,
            s.description,
            s.scheduledDate,
            s.completedDate,
            s.status,
            s.createdAt,
            CONCAT(u.firstName, ' ', u.lastName),
            a.assetName,
            a.status
        )
        FROM AssetService s
        JOIN s.asset a
        JOIN s.raisedBy u
        WHERE (:status IS NULL OR s.status = :status)
    """)
    List<AssetServiceDTO> getAllServiceComplaints(@Param("status") AssetServiceStatus status);
}
