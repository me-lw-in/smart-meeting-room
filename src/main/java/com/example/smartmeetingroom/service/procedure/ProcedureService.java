package com.example.smartmeetingroom.service.procedure;

import com.example.smartmeetingroom.dto.procedure.ProcedureDTO;
import com.example.smartmeetingroom.dto.procedure.UpdateProcedureRequestDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProcedureService {
     void createProcedure(ProcedureDTO request);
     String linkAssetsToProcedure(Long procedureId,String action, List<Long> assetIds);
     void updateProcedure(Long procedureId, UpdateProcedureRequestDTO request);
     Page<ProcedureDTO> getAllProcedures(int page, int size, String search);
     void deleteProcedure(Long procedureId);
}
