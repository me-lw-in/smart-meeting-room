package com.example.smartmeetingroom.service.cache;

import com.example.smartmeetingroom.dto.procedure.ProcedureDTO;

import java.util.List;
import java.util.Set;

public interface ProcedureCacheService {

    List<ProcedureDTO> get(Long assetId);

    void put(Long assetId, List<ProcedureDTO> procedures);

    void evict(Set<Long> assetIds);
}
