package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.asset.AssetDTO;
import com.example.smartmeetingroom.service.asset.AssetService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/assets")
class AssetController {

    private final AssetService assetService;

    @PostMapping
    public ResponseEntity<Void> addAsset(@RequestBody @Valid AssetDTO dto){
        assetService.addAsset(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{assetId}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long assetId, @RequestParam(required = false) Long meetingRoomId) {
        assetService.deleteAsset(assetId, meetingRoomId);
        return ResponseEntity.noContent().build();
    }
}
