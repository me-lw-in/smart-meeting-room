package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.asset.AssetDTO;
import com.example.smartmeetingroom.dto.page.PageResponseDTO;
import com.example.smartmeetingroom.enums.AssetStatus;
import com.example.smartmeetingroom.service.asset.AssetService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
