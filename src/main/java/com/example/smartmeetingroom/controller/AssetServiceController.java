package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.assetservice.AssetServiceDTO;
import com.example.smartmeetingroom.dto.assetservice.CreateAssetTicketDTO;
import com.example.smartmeetingroom.enums.AssetServiceStatus;
import com.example.smartmeetingroom.service.assetservice.AssetServiceServ;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/asset-service")
class AssetServiceController {

    private final AssetServiceServ assetServiceServ;

    @PostMapping
    public ResponseEntity<String> raiseComplaint(@RequestBody @Valid CreateAssetTicketDTO dto) {
        assetServiceServ.raiseComplaint(dto);
        return ResponseEntity.ok("Service request created");
    }

    @GetMapping
    public ResponseEntity<List<AssetServiceDTO>> getAllAssetComplaints(@RequestParam(required = false) AssetServiceStatus status) {
        var body  = assetServiceServ.getAllAssetComplaints(status);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/{id}/decision")
    public ResponseEntity<Void> makeDecisionOnComplaint(@PathVariable Long id, @RequestBody @Valid AssetServiceDTO dto) {
        assetServiceServ.makeDecisionOnComplaint(id, dto);
        return ResponseEntity.ok().build();
    }
}
