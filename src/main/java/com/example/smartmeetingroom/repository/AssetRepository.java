package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {
    boolean existsByAssetNameAndAssetTypeIdNot(String deviceName, Short deviceTypeId);

    boolean existsByAssetType_Id(Short assetTypeId);

    boolean existsBySerialNumber(String serialNumber);

    Optional<Asset> findByIdAndRoom_Id(Long id, Long roomId);
}
