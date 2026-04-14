package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.assetservice.CreateAssetTicketDTO;
import com.example.smartmeetingroom.service.assetservice.AssetServiceServ;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@AllArgsConstructor
@RequestMapping("/api/asset-service")
class AssetServiceController {

    private final AssetServiceServ assetServiceServ;

    @PostMapping
    public ResponseEntity<String> createTicket(@RequestBody @Valid CreateAssetTicketDTO dto) {
        assetServiceServ.createTicket(dto);
        return ResponseEntity.ok("Service request created");
    }
}
