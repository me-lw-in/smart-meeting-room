package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.device.DeviceDTO;
import com.example.smartmeetingroom.service.device.DeviceService;
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
@RequestMapping("/api/devices")
class DeviceController {

    private final DeviceService deviceService;

    @PostMapping()
    public ResponseEntity<Void> addDevice(@RequestBody @Valid DeviceDTO dto){
        deviceService.addDevice(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
