package com.example.smartmeetingroom.service.technician;

import com.example.smartmeetingroom.dto.technician.TechnicianTaskResponseDTO;
import com.example.smartmeetingroom.repository.AssetServiceRepository;
import com.example.smartmeetingroom.repository.ProcedureRepository;
import com.example.smartmeetingroom.util.SecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class TechnicianServiceImpl implements TechnicianService {

    private final AssetServiceRepository assetServiceRepository;
    private final ProcedureRepository procedureRepository;

    @Override
    public TechnicianTaskResponseDTO getAssignedTask() {
        var technicianId = SecurityUtil.getCurrentUserId();
        var isTechnician = "TECHNICIAN".equalsIgnoreCase(SecurityUtil.getCurrentUserRole());

        if (technicianId == null || !isTechnician) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }

        var assignedTasks = assetServiceRepository.getTechnicianAssignedTask(technicianId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No task assigned"));
        var assetProcedure = procedureRepository.getAssetProcedures(assignedTasks.getAssetId());
        assignedTasks.setProcedures(assetProcedure);
        return assignedTasks;
    }
}
