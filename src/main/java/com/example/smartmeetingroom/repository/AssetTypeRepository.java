package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.entity.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetTypeRepository extends JpaRepository<AssetType, Short> {
    boolean existsByName(String name);
}
