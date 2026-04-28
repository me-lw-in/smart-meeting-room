package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.dto.asset.AssetDTO;
import com.example.smartmeetingroom.entity.Asset;
import com.example.smartmeetingroom.enums.AssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {

    Optional<Asset> findBySerialNumber(String serialNumber);

    Optional<Asset> findByIdAndIsDeletedFalse(Long id);

    boolean existsByAssetNameAndAssetTypeIdNot(String deviceName, Short deviceTypeId);

    boolean existsByAssetType_Id(Short assetTypeId);

    boolean existsBySerialNumber(String serialNumber);

    Optional<Asset> findByIdAndRoom_IdAndIsDeletedFalse(Long id, Long roomId);

    @Modifying
    @Query("UPDATE Asset a SET a.status = :newStatus WHERE a.room.id IN :roomIds AND a.status = :currentStatus")
    int updateStatusByRoomAndCurrentStatus(
            List<Long> roomIds,
            AssetStatus currentStatus,
            AssetStatus newStatus
    );

    boolean existsBySerialNumberAndIdNot(String serialNumber, Long id);

    boolean existsByAssetNameAndAssetTypeIdNotAndIdNot(String assetName, Short assetTypeId, Long id);

    @Query("""
        SELECT a.room.id, COUNT(a)
        FROM Asset a
        WHERE a.room.id IN :roomIds
        AND a.isDeleted = false
        GROUP BY a.room.id
    """)
    List<Object[]> getTotalAssets(Set<Long> roomIds);

    @Query("""
        SELECT a.room.id,
        SUM(CASE WHEN a.status IN ('ACTIVE','AVAILABLE','IN_USE') THEN 1 ELSE 0 END),
        SUM(CASE WHEN a.status NOT IN ('ACTIVE','AVAILABLE','IN_USE') THEN 1 ELSE 0 END)
        FROM Asset a
        WHERE a.room.id IN :roomIds
        AND a.isDeleted = false
        GROUP BY a.room.id
    """)
    List<Object[]> getAssetStats(Set<Long> roomIds);

    @Query("""
        SELECT a.room.id, a.assetName, COUNT(a)
        FROM Asset a
        WHERE a.room.id IN :roomIds
        AND a.isDeleted = false
        GROUP BY a.room.id, a.assetName
    """)
    List<Object[]> getAssetsByName(Set<Long> roomIds);

    @Query("""
        SELECT new com.example.smartmeetingroom.dto.asset.AssetDTO(
            a.assetName,
            a.serialNumber,
            a.purchaseDate,
            a.warrantyExpiry,
            r.roomName,
            at.name,
            a.status
        )
        FROM Asset a
        JOIN a.room r
        JOIN a.assetType at
    """)
    List<AssetDTO> getAllAssets();

}
