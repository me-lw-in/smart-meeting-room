package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.assettype.AssetTypeDTO;
import com.example.smartmeetingroom.entity.AssetType;
import com.example.smartmeetingroom.service.assettype.AssetTypeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/asset-types")
class AssetTypeController {

    private final AssetTypeService assetTypeService;

    @PostMapping()
    public ResponseEntity<Void> addAssetType(@RequestBody @Valid AssetTypeDTO dto){
        assetTypeService.addAssetType(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<AssetType>> getAllAssetTypes() {
        var assetTypes = assetTypeService.getAllAssetTypes();
        return ResponseEntity.ok(assetTypes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssetType(@PathVariable Short id) {
        assetTypeService.deleteAssetType(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateDeviceType(@PathVariable Short id, @RequestBody @Valid AssetTypeDTO dto) {
        assetTypeService.updateAssetType(id, dto);
        return ResponseEntity.noContent().build();
    }
}
