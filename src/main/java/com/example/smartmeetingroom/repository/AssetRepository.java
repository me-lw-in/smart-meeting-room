package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    boolean existsByAssetNameAndAssetTypeIdNot(String deviceName, Short deviceTypeId);

    boolean existsByAssetType_Id(Short assetTypeId);

    boolean existsBySerialNumber(String serialNumber);

    Optional<Asset> findByIdAndRoom_Id(Long id, Long roomId);
}
