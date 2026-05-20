package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.technician.UpdateAssetServiceDTO;
import com.example.smartmeetingroom.dto.technician.TechnicianTaskResponseDTO;
import com.example.smartmeetingroom.service.assetservice.AssetServiceServ;
import com.example.smartmeetingroom.service.technician.TechnicianService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @PatchMapping("/tasks/{complaintId}/status")
    public ResponseEntity<?> updateAssetServiceStatus(@PathVariable Long complaintId, @RequestBody UpdateAssetServiceDTO dto) {
        var status = dto.getServiceStatus().toUpperCase();
        switch (status){
            case "START" -> assetServiceServ.startAssetService(complaintId, dto);
            case "REJECT" ->  assetServiceServ.rejectComplaint(complaintId, dto.getRemarks());
            case "RESOLVED", "FAILED" ->  assetServiceServ.completeAssetService(complaintId, dto);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status.");
        }
        return ResponseEntity.ok().body("Service status updated successfully");
    }
}
