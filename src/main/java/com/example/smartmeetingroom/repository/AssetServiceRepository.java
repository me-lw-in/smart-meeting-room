package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.entity.AssetService;
import com.example.smartmeetingroom.enums.AssetServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface AssetServiceRepository extends JpaRepository<AssetService, Long> {
    boolean existsByAssetIdAndStatusIn(Long assetId, Collection<AssetServiceStatus> statuses);
}
