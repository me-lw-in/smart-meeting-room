package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.entity.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceTypeRepository extends JpaRepository<DeviceType, Short> {
    boolean existsByName(String name);
}
