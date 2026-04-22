package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.technician.CompleteTaskRequestDTO;
import com.example.smartmeetingroom.dto.technician.TechnicianTaskResponseDTO;
import com.example.smartmeetingroom.service.assetservice.AssetServiceServ;
import com.example.smartmeetingroom.service.technician.TechnicianService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/techniciain")
class TechnicianController {

    private final TechnicianService technicianService;
    private final AssetServiceServ assetServiceServ;

    @GetMapping("/tasks")
    public ResponseEntity<TechnicianTaskResponseDTO> getAssignedTasks() {
        return ResponseEntity.ok(technicianService.getAssignedTask());
    }

    @PatchMapping("/tasks/{complaintId}/start")
    public ResponseEntity<String> startService(@PathVariable Long complaintId) {
        assetServiceServ.startAssetService(complaintId);
        return ResponseEntity.ok("Service started.");
    }

    @PatchMapping("/tasks/{complaintId}/complete")
    public ResponseEntity<Void> finishService(@PathVariable Long complaintId, @RequestBody CompleteTaskRequestDTO dto) {
        assetServiceServ.completeAssetService(complaintId, dto);
        return ResponseEntity.noContent().build();
    }
}
