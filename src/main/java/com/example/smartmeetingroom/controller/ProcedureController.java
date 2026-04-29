package com.example.smartmeetingroom.controller;

import com.example.smartmeetingroom.dto.procedure.LinkAssetsToProcedureRequestDTO;
import com.example.smartmeetingroom.dto.procedure.ProcedureDTO;
import com.example.smartmeetingroom.dto.procedure.UpdateProcedureRequestDTO;
import com.example.smartmeetingroom.service.procedure.ProcedureService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/procedures")
@AllArgsConstructor
class ProcedureController {

    private final ProcedureService procedureService;

    @GetMapping
    public ResponseEntity<?> getAllProcedures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        var result = procedureService.getAllProcedures(page, size, search);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<String> createProcedure(@Valid @RequestBody ProcedureDTO request) {
        procedureService.createProcedure(request);
        return ResponseEntity.ok("Procedure created successfully");
    }

    @PatchMapping("/{procedureId}/assets")
    public ResponseEntity<?> linkAssets(
            @PathVariable Long procedureId,
            @Valid @RequestBody LinkAssetsToProcedureRequestDTO request) {

        procedureService.linkAssetsToProcedure(procedureId, request.getAssetIds());
        return ResponseEntity.ok(Map.of(
                "message", "Assets linked to procedure successfully"
        ));
    }

    @PatchMapping("/{procedureId}")
    public ResponseEntity<?> updateProcedure(
            @PathVariable Long procedureId,
            @RequestBody UpdateProcedureRequestDTO request) {

        procedureService.updateProcedure(procedureId, request);

        return ResponseEntity.ok(Map.of(
                "message", "Procedure updated successfully"
        ));
    }

    @DeleteMapping("/{procedureId}")
    public ResponseEntity<?> deleteProcedure(@PathVariable Long procedureId) {

        procedureService.deleteProcedure(procedureId);

        return ResponseEntity.ok(Map.of(
                "message", "Procedure deleted successfully"
        ));
    }
}
