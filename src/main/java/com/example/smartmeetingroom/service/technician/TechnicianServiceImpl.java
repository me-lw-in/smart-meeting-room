package com.example.smartmeetingroom.service.technician;

import com.example.smartmeetingroom.dto.procedure.ProcedureDTO;
import com.example.smartmeetingroom.dto.technician.TechnicianTaskResponseDTO;
import com.example.smartmeetingroom.repository.AssetServiceRepository;
import com.example.smartmeetingroom.repository.ProcedureRepository;
import com.example.smartmeetingroom.service.cache.ProcedureCacheService;
import com.example.smartmeetingroom.util.SecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class TechnicianServiceImpl implements TechnicianService {

    private final AssetServiceRepository assetServiceRepository;
    private final ProcedureRepository procedureRepository;
    private final ProcedureCacheService cacheService;

    @Override
    public TechnicianTaskResponseDTO getAssignedTask() {
        var technicianId = SecurityUtil.getCurrentUserId();
        var isTechnician = "TECHNICIAN".equalsIgnoreCase(SecurityUtil.getCurrentUserRole());

        if (technicianId == null || !isTechnician) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }

        var assignedTasks = assetServiceRepository.getTechnicianAssignedTask(technicianId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No task assigned"));

        // Check cache
        var cached = cacheService.get(assignedTasks.getAssetId());
        List<ProcedureDTO> assetProcedure;
        if (cached != null) {
            assignedTasks.setProcedures(cached); // ⚡ fast return
        }else {
            assetProcedure = procedureRepository.getAssetProcedures(assignedTasks.getAssetId());
            assignedTasks.setProcedures(assetProcedure);
            cacheService.put(assignedTasks.getAssetId(), assetProcedure);
        }
        return assignedTasks;
    }
}
