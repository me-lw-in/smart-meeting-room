package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.devicetype.DeviceTypeDTO;
import com.example.smartmeetingroom.service.devicetype.DeviceTypeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@AllArgsConstructor
@RequestMapping("/api/device-types")
class DeviceTypeController {

    private final DeviceTypeService deviceTypeService;

    @PostMapping()
    public ResponseEntity<Void> addDeviceType(@RequestBody @Valid DeviceTypeDTO dto){
        deviceTypeService.addDeviceType(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
