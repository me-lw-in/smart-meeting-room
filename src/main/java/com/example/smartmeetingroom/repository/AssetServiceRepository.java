package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.dto.assetservice.AssetServiceDTO;
import com.example.smartmeetingroom.dto.technician.TechnicianTaskResponseDTO;
import com.example.smartmeetingroom.entity.AssetService;
import com.example.smartmeetingroom.enums.AssetServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AssetServiceRepository extends JpaRepository<AssetService, Long> {
    boolean existsByAssetIdAndStatusIn(Long assetId, Collection<AssetServiceStatus> statuses);

    @Query("""
        SELECT new com.example.smartmeetingroom.dto.assetservice.AssetServiceDTO(
            s.id,
            s.description,
            s.scheduledDate,
            s.startedAt,
            s.completedAt,
            s.status,
            s.createdAt,
            CONCAT(u.firstName, ' ', u.lastName),
            CONCAT(t.firstName, ' ', t.lastName),
            a.assetName,
            a.status
        )
        FROM AssetService s
        JOIN s.asset a
        JOIN s.raisedBy u
        LEFT JOIN s.technician t
        WHERE (:status IS NULL OR s.status = :status)
    """)
    List<AssetServiceDTO> getAllServiceComplaints(@Param("status") AssetServiceStatus status);

    @Query("""
        SELECT new com.example.smartmeetingroom.dto.technician.TechnicianTaskResponseDTO(
            c.id,
            c.description,
            c.scheduledDate,
            c.status,
            a.id,
            a.assetName,
            at.name,
            a.serialNumber,
            a.status,
            m.roomName,
            c.reviewedAt
            )
        FROM AssetService c
        JOIN c.asset a
        JOIN a.assetType at
        JOIN a.room m
        WHERE c.technician.id = :technicianId AND c.status = AssetServiceStatus.ASSIGNED
    
    """)
    Optional<TechnicianTaskResponseDTO> getTechnicianAssignedTask(Long technicianId);
}
