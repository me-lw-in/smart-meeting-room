package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.asset.AssetDTO;
import com.example.smartmeetingroom.dto.page.PageResponseDTO;
import com.example.smartmeetingroom.enums.AssetStatus;
import com.example.smartmeetingroom.service.asset.AssetService;
import com.example.smartmeetingroom.util.SecurityUtil;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/assets")
class AssetController {

    private final AssetService assetService;

    @PostMapping
    public ResponseEntity<Void> addAsset(@RequestBody @Valid AssetDTO dto){
        log.info("Add asset request received for assetName: {} by userId: {}", dto.getAssetName(), SecurityUtil.getCurrentUserId());
        assetService.addAsset(dto);
        log.info("Asset created successfully: {}", dto.getAssetName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{assetId}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long assetId, @RequestParam(required = false) Long meetingRoomId) {
        log.info("Delete asset request received for assetId: {}, meetingRoomId: {} by userId: {}",
                assetId, meetingRoomId, SecurityUtil.getCurrentUserId());
        assetService.deleteAsset(assetId, meetingRoomId);
        log.info("Asset deleted successfully for assetId: {}", assetId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<AssetDTO>> getAllAssets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Short typeId,
            @RequestParam(required = false) Long meetingRoomId,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) List<String> sort
    ) {
        var content =  assetService.getAllAssets(page,size,search,typeId,meetingRoomId,status,sort);
        return ResponseEntity.ok(content);
    }

    @PatchMapping("/{assetId}")
    public ResponseEntity<Void> updateAsset(@PathVariable Long assetId, @RequestBody JsonNode request) {
        log.info("Update asset request received for assetId: {} by userId: {}", assetId, SecurityUtil.getCurrentUserId());
        assetService.updateAsset(assetId, request);
        log.info("Asset updated successfully for assetId: {}", assetId);
        return ResponseEntity.noContent().build();
    }
}
