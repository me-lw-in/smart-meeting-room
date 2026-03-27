package com.example.smartmeetingroom.repository;

import com.example.smartmeetingroom.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    boolean existsByDeviceNameAndDeviceTypeIdNot(String deviceName, Short deviceTypeId);
}
